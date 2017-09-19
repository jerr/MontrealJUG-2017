package org.printstacktrace.demo.message.service;

import java.time.Instant;
import java.util.Map;

import com.netflix.hystrix.HystrixInvokableInfo;
import com.netflix.ribbon.hystrix.FallbackHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import rx.Observable;

public class TimeFallbackHandler implements FallbackHandler<ByteBuf> {

	@Override
	public Observable<ByteBuf> getFallback(HystrixInvokableInfo<?> hystrixInfo, Map<String, Object> requestProps) {
		String fallback = "" + Instant.now().toEpochMilli();
		byte[] bytes = fallback.getBytes();
		ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer(bytes.length);
		byteBuf.writeBytes(bytes);
		return Observable.just(byteBuf);
	}

}
