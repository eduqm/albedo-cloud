/*
 *  Copyright (c) 2019-2020, somewhere (somewhere0813@gmail.com).
 *  <p>
 *  Licensed under the GNU Lesser General Public License 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 * https://www.gnu.org/licenses/lgpl.html
 *  <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.albedo.java.common.core.exception.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.albedo.java.common.core.exception.*;
import com.albedo.java.common.core.util.BeanValidators;
import com.albedo.java.common.core.util.Json;
import com.albedo.java.common.core.util.ResponseEntityBuilder;
import com.albedo.java.common.core.util.Result;
import com.alibaba.fastjson.JSONObject;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * @author somewhere
 * @date 2019/2/1
 * 全局的的异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	/**
	 * 全局异常.
	 *
	 * @param e the e
	 * @return R
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Result> exception(Exception e) {
//		if(e instanceof RpcException){
//			try {
//				String method = e.getMessage().substring(e.getMessage().indexOf("method") + 6, e.getMessage().indexOf(" in "));
//				return Result.buildFail("远程服务["+method+"]调用失败");
//			}catch (Exception e1){
//				return Result.buildFail("远程服务调用失败");
//			}
//		}
		if(e instanceof HystrixRuntimeException){
			try {
				if(e.getCause() instanceof feign.FeignException){
					return ResponseEntityBuilder.build(Json.parseObject(((feign.FeignException) e.getCause()).contentUTF8(), Result.class));
				}
			}catch (Exception e1){
				return ResponseEntityBuilder.buildFail("远程服务调用失败");
			}
		}
		log.error("全局异常信息 ex={}", e);
		return ResponseEntityBuilder.buildFail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
	}
	/**
	 * validation Exception
	 *
	 * @param exception
	 * @return R
	 */
	@ExceptionHandler({FeignException.class})
	@ResponseStatus(BAD_REQUEST)
	public Result feignExceptionHandler(FeignException exception) {
		log.warn("FeignException:{}", exception);
		return Result.buildFail("远程服务调用失败");
	}
	/**
	 * validation Exception
	 *
	 * @param exception
	 * @return R
	 */
	@ExceptionHandler({AccessDeniedException.class})
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public Result bodyValidExceptionHandler(AccessDeniedException exception) {
		log.warn("AccessDeniedException:{}", exception);
		return Result.buildFail("权限不足");
	}

	/**
	 * validation Exception
	 *
	 * @param exception
	 * @return R
	 */
	@ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
	@ResponseStatus(BAD_REQUEST)
	public Result bodyValidExceptionHandler(MethodArgumentNotValidException exception) {
		List<String> messages = BeanValidators.extractPropertyAndMessageAsList(exception);
		log.warn("Valid Error:" + messages);
		return Result.buildFail(ArrayUtil.toArray(messages, String.class));
	}

	/**
	 * RuntimeMsgException
	 *
	 * @param exception
	 * @return R
	 */
	@ExceptionHandler({RuntimeMsgException.class})
	public Result bodyRuntimeMsgExceptionHandler(RuntimeMsgException exception) {
		log.error("runtime msg={}", exception.getMessage(), exception);
		return Result.buildFail(exception.getMessage());
	}

	/**
	 * 处理 badException
	 */
	@ExceptionHandler(value = {BadRequestException.class, EntityExistException.class, IllegalArgumentException.class})
	public ResponseEntity<Result> badException(Exception e) {
		// 打印堆栈信息
		log.warn(ExceptionUtil.stacktraceToString(e));
		return ResponseEntityBuilder.buildFail(BAD_REQUEST, e.getMessage());
	}

	/**
	 * 处理 EntityNotFound
	 */
	@ExceptionHandler(value = EntityNotFoundException.class)
	public ResponseEntity<Result> entityNotFoundException(EntityNotFoundException e) {
		// 打印堆栈信息
		log.warn(ExceptionUtil.stacktraceToString(e));
		return ResponseEntityBuilder.buildFail(NOT_FOUND, e.getMessage());
	}
}
