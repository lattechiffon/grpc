package com.lattechiffon.grpc;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

public class UserServerInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        System.out.println("[Server Interceptor] : Invoke RPC - " + call.getMethodDescriptor().getFullMethodName());
        ServerCall<ReqT, RespT> serverCall = new UserServerCall<>(call);

        return new UserServerCallListener<>(next.startCall(serverCall, headers));
    }
}
