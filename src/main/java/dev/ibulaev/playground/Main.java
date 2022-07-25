package dev.ibulaev.playground;

import lombok.SneakyThrows;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        errorImitation();
    }

    private static void subscriberOnObservable() {
        // имитация активного продьюсера (через пуш)
        Flux<Object> telegramProducer = Flux.generate(
                () -> 2354,
                (state, sink) -> {
                    if (state > 2366) {
                        sink.complete();
                    } else {
                        sink.next("Step: " + state);
                    }

                    return state + 3;
                }
        );

        Flux.create(sink ->
                telegramProducer.subscribe(new BaseSubscriber<Object>() {
                    @Override
                    protected void hookOnNext(Object value) {
                        sink.next(value);
                    }

                    @Override
                    protected void hookOnComplete() {
                        sink.complete();
                    }
                }))
                .subscribe(System.out::println);
    }

    private static void subscriberOnRequest() {
        // имитация подписки через pull
        Flux<Object> telegramProducer = Flux.generate(
                () -> 2354,
                (state, sink) -> {
                    if (state > 2366) {
                        sink.complete();
                    } else {
                        sink.next("Step: " + state);
                    }

                    return state + 3;
                }
        );

        Flux.create(sink ->
                sink.onRequest(r -> {
                    sink.next("DB returns: " + telegramProducer.blockFirst());
                }))
                .subscribe(System.out::println);
    }

    private static void concatinationOfTwoFlux() {
        Flux<String> second = Flux.just("World", "coder").repeat();
        Flux<String> sumFlux = Flux.just("Hello", "dru", "java", "Linux", "Asia")
                .zipWith(second, (f, s) -> String.format("%s %s", f, s));

        sumFlux.subscribe(System.out::println);
    }

    @SneakyThrows
    private static void errorImitation() {
        Flux<String> second = Flux.just("World", "coder").repeat();
        Flux<String> sumFlux = Flux.just("Hello", "dru", "java", "Linux", "Asia")
                .zipWith(second, (f, s) -> String.format("%s %s", f, s));

//        sumFlux
//                .delayElements(Duration.ofMillis(1300))
//                .timeout(Duration.ofSeconds(1))
//                .onErrorReturn("Too slow")
//                .subscribe(System.out::println);

        Flux<String> stringFlux = sumFlux
                .delayElements(Duration.ofMillis(1300))
                .timeout(Duration.ofSeconds(1))
                .onErrorResume(throwable ->
                        Flux.interval(Duration.ofMillis(300))
                                .map(String::valueOf)
                )
                .skip(2)
                .take(3);

        stringFlux
                .subscribe(
                        System.out::println,
                        System.out::println,
                        () -> System.out.println("Finished")
                );

        Thread.sleep(4000);
    }
}
