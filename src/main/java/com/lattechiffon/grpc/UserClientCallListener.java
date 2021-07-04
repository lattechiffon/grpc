package com.lattechiffon.grpc;

import io.grpc.ClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;

public class UserClientCallListener<RespT> extends ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT> {
    protected UserClientCallListener(ClientCall.Listener<RespT> delegate) {
        super(delegate);
    }

    @Override
    public void onHeaders(Metadata headers) {
        System.out.println("[Client Call Listener] client header : " + headers);
        super.onHeaders(headers);
    }
}
