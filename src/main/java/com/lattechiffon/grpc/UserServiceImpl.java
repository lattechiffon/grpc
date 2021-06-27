package com.lattechiffon.grpc;

import io.grpc.Status;
import io.grpc.StatusException;

import java.util.HashMap;
import java.util.Map;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private final Map<Long, User> userMap = new HashMap<>();

    private long idxCounter = 1;

    @Override
    public void setUser(User request, io.grpc.stub.StreamObserver<UserIdx> responseObserver) {
        request = request.toBuilder().setIdx(idxCounter++).build();
        userMap.put(request.getIdx(), request);

        UserIdx response = UserIdx.newBuilder().setIdx(request.getIdx()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUser(UserIdx request, io.grpc.stub.StreamObserver<User> responseObserver) {
        long userIdx = request.getIdx();

        if (userMap.containsKey(userIdx)) {
            responseObserver.onNext(userMap.get(userIdx));
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
        }
    }
}
