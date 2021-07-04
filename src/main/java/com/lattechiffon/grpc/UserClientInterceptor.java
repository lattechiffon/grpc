package com.lattechiffon.grpc;

import io.grpc.*;

public class UserClientInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new UserClientCall<>(next.newCall(method, callOptions));
    }
}
