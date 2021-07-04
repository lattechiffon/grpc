package com.lattechiffon.grpc;

import io.grpc.ForwardingServerCall;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;

public class UserServerCall<ReqT, RespT> extends ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT> {

    protected UserServerCall(ServerCall<ReqT, RespT> delegate) {
        super(delegate);
    }

    @Override
    protected ServerCall<ReqT, RespT> delegate() {
        return super.delegate();
    }

    @Override
    public MethodDescriptor<ReqT, RespT> getMethodDescriptor() {
        return super.getMethodDescriptor();
    }

    @Override
    public void sendMessage(RespT message) {
        System.out.println("[Server Call] Service Return Message : " + message);
        super.sendMessage(message);
    }
}
