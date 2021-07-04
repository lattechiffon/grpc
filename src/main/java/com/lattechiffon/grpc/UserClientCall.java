package com.lattechiffon.grpc;

import io.grpc.ClientCall;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;

public class UserClientCall<ReqT, RespT> extends ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT> {
    protected UserClientCall(ClientCall<ReqT, RespT> delegate) {
        super(delegate);
    }

    @Override
    public void start(Listener<RespT> responseListener, Metadata headers) {
        super.start(new UserClientCallListener<>(responseListener), headers);
    }

    @Override
    public void sendMessage(ReqT message) {
        System.out.println("[Client Call] Client Send Message : " + message);
        super.sendMessage(message);
    }
}
