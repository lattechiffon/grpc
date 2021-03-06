package com.lattechiffon.grpc;

import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GrpcApplication {

    public static void main(String[] args) {
        // Initialize gRPC Server
        int port = 8080;
        Server server = ServerBuilder.forPort(port)
                .addService(ServerInterceptors.intercept(new UserServiceImpl(), new UserServerInterceptor())).build();

        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Server: Shutting down gRPC server");
            server.shutdown();
            System.err.println("Server: Server shut down");
        }));

        // Initialize gRPC Client
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext()
                .intercept(new UserClientInterceptor()).build();
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel).withDeadlineAfter(500, TimeUnit.MILLISECONDS);
        UserServiceGrpc.UserServiceStub asyncStub = UserServiceGrpc.newStub(channel).withDeadlineAfter(3000, TimeUnit.MILLISECONDS);
        UserServiceGrpc.UserServiceFutureStub futureStub = UserServiceGrpc.newFutureStub(channel);

        // Client: Unary RPC, 3 times
        System.out.println("(1) Unary RPC");
        UserIdx setUserResult = stub.setUser(User.newBuilder().setUsername("GO YONGGUK")
                .setEmail("lattechiffon@gmail.com").addRoles("USER").addRoles("ADMIN").build());
        // System.out.println("Client: " + setUserResult.getIdx(0));

        setUserResult = stub.setUser(User.newBuilder().setUsername("KIM MINSU")
                .setEmail("minsu@test.com").addRoles("USER").build());
        // System.out.println("Client: " + setUserResult.getIdx(0));

        User getUserResult = stub.getUser(setUserResult);
        // System.out.println(getUserResult.toString());

        // Client: Client-side Streaming RPC
        System.out.println("(2) Client-side Streaming RPC");

        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<UserIdx> responseObserver = new StreamObserver<UserIdx>() {

            @Override
            public void onNext(UserIdx userIdx) {
                for (long idx : userIdx.getIdxList()) {
                    // System.out.println("Client: " + idx);
                }
            }

            @Override
            public void onError(Throwable t) {
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        StreamObserver<User> requestObserver = asyncStub.setUsers(responseObserver);

        try {
            for (int i = 0; i < 5; i++) {
                requestObserver.onNext(User.newBuilder().setUsername("NEW USER - " + i)
                        .setEmail("test@test.com").addRoles("USER").build());
                Thread.sleep(500);
            }
        } catch (StatusRuntimeException|InterruptedException e) { System.out.println("Deadline Exceeded. : " + e.getMessage()); }

        requestObserver.onCompleted();

        try {
            finishLatch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) { }

        // Client: Server-side Streaming RPC
        System.out.println("(3) Server-side Streaming RPC");

        try {
            Iterator<User> getUsersResult = stub.getUsers(UserIdx.newBuilder().addIdx(1).addIdx(2).build());

            while (getUsersResult.hasNext()) {
                getUsersResult.next();
                //System.out.println(getUsersResult.next().toString());
            }
        } catch (StatusRuntimeException e) { System.out.println("Deadline Exceeded. : " + e.getMessage()); }

        // Client: Bidirectional Streaming RPC
        System.out.println("(4) Bidirectional Streaming RPC");

        final CountDownLatch finishLatch2 = new CountDownLatch(1);
        StreamObserver<User> responseObserver2 = new StreamObserver<User>() {

            @Override
            public void onNext(User user) {
                // System.out.println(user.toString());
            }

            @Override
            public void onError(Throwable t) {
                finishLatch2.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch2.countDown();
            }
        };

        StreamObserver<UserIdx> requestObserver2 = asyncStub.getUsersRealtime(responseObserver2);

        try {
            for (int i = 1; i <= 5; i++) {
                requestObserver2.onNext(UserIdx.newBuilder().addIdx(i).build());
                Thread.sleep(1000);
            }

            requestObserver2.onNext(UserIdx.newBuilder().addIdx(6).addIdx(7).build());
        } catch (StatusRuntimeException|InterruptedException e) { System.out.println("Deadline Exceeded. : " + e.getMessage()); }

        requestObserver2.onCompleted();

        try {
            finishLatch2.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException ignored) { }

        // Deadline
        try {
            User testForDeadline = stub.getUserSlowly(UserIdx.newBuilder().addIdx(0).build());
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                System.out.println("Deadline Exceeded. : " + e.getMessage());
            }
        }

        // Release
        channel.shutdown();
        Runtime.getRuntime().exit(0);
    }
}
