package com.lattechiffon.grpc;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

import java.util.HashMap;
import java.util.Map;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private final Map<Long, User> userMap = new HashMap<>();

    private long idxCounter = 1;

    @Override
    public void setUser(User request, StreamObserver<UserIdx> responseObserver) {
        request = request.toBuilder().setIdx(idxCounter++).build();
        userMap.put(request.getIdx(), request);

        UserIdx response = UserIdx.newBuilder().addIdx(request.getIdx()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUser(UserIdx request, StreamObserver<User> responseObserver) {
        long userIdx = request.getIdx(0);

        if (userMap.containsKey(userIdx)) {
            responseObserver.onNext(userMap.get(userIdx));
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
        }
    }

    @Override
    public void getUserSlowly(UserIdx request, StreamObserver<User> responseObserver) {
        long userIdx = request.getIdx(0);

        if (userMap.containsKey(userIdx)) {
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ignored) { };
            responseObserver.onNext(userMap.get(userIdx));
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(new StatusException(Status.NOT_FOUND));
        }
    }

    @Override
    public StreamObserver<User> setUsers(final StreamObserver<UserIdx> responseObserver) {
        return new StreamObserver<User>() {

            final UserIdx.Builder responseBuilder = UserIdx.newBuilder();

            @Override
            public void onNext(User user) {
                user = user.toBuilder().setIdx(idxCounter++).build();
                userMap.put(user.getIdx(), user);
                responseBuilder.addIdx(user.getIdx());
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(responseBuilder.build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void getUsers(UserIdx request, StreamObserver<User> responseObserver) {

        for (long idx : request.getIdxList()) {
            if (userMap.containsKey(idx)) {
                responseObserver.onNext(userMap.get(idx));
            } else {
                responseObserver.onError(new StatusException(Status.NOT_FOUND));
            }
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<UserIdx> getUsersRealtime(StreamObserver<User> responseObserver) {
        return new StreamObserver<UserIdx>() {

            @Override
            public void onNext(UserIdx userIdx) {
                for (long idx : userIdx.getIdxList()) {
                    if (userMap.containsKey(idx)) {
                        responseObserver.onNext(userMap.get(idx));
                    } else {
                        responseObserver.onError(new StatusException(Status.NOT_FOUND));
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
