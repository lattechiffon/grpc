package com.lattechiffon.grpc;

import io.grpc.ForwardingServerCallListener;
import io.grpc.ServerCall;

public class UserServerCallListener<ReqT> extends ForwardingServerCallListener<ReqT> {

    private final ServerCall.Listener<ReqT> delegate;

    UserServerCallListener(ServerCall.Listener<ReqT> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected ServerCall.Listener<ReqT> delegate() {
        return delegate;
    }

    @Override
    public void onMessage(ReqT message) {
        System.out.println("[Server Call Listener] Client Message : " + message);
        super.onMessage(message);
    }
}
