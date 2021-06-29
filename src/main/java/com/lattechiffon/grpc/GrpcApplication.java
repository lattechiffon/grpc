package com.lattechiffon.grpc;

import io.grpc.*;

import java.io.IOException;
import java.util.Iterator;

public class GrpcApplication {

    public static void main(String[] args) {
        // Initialize gRPC Server
        int port = 8080;
        Server server = ServerBuilder.forPort(port).addService(new UserServiceImpl()).build();

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
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build();
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);
        UserServiceGrpc.UserServiceStub asyncStub = UserServiceGrpc.newStub(channel);
        UserServiceGrpc.UserServiceFutureStub futureStub = UserServiceGrpc.newFutureStub(channel);

        // Client: Unary RPC, 3 times
        System.out.println("(1) Unary RPC");
        UserIdx setUserResult = stub.setUser(User.newBuilder().setUsername("GO YONGGUK")
                .setEmail("lattechiffon@gmail.com").addRoles("USER").addRoles("ADMIN").build());
        System.out.println("Client: " + setUserResult.getIdx(0));

        setUserResult = stub.setUser(User.newBuilder().setUsername("KIM MINSU")
                .setEmail("minsu@test.com").addRoles("USER").build());
        System.out.println("Client: " + setUserResult.getIdx(0));

        User getUserResult = stub.getUser(setUserResult);
        System.out.println(getUserResult.toString());

        // Client: Client-side Streaming RPC
        System.out.println("(2) Client-side Streaming RPC");

        // Client: Server-side Streaming RPC
        System.out.println("(3) Server-side Streaming RPC");

        try {
            Iterator<User> getUsersResult = stub.getUsers(UserIdx.newBuilder().addIdx(1).addIdx(2).build());

            while (getUsersResult.hasNext()) {
                System.out.println(getUsersResult.next().toString());
            }
        } catch (StatusRuntimeException ignored) { }

        // Client: Bidirectional Streaming RPC
        System.out.println("(4) Bidirectional Streaming RPC");

        // Release
        channel.shutdown();
        Runtime.getRuntime().exit(0);
    }
}
