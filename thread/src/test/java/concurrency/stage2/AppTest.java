package concurrency.stage2;

import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class AppTest {

    private static final AtomicInteger count = new AtomicInteger(0);

    /**
     * 1. App 클래스의 애플리케이션을 실행시켜 서버를 띄운다.
     * 2. 아래 테스트를 실행시킨다.
     * 3. AppTest가 아닌 App의 콘솔에서 SampleController가 생성한 http call count 로그를 확인한다.
     * 4. application.yml에서 설정값을 변경해보면서 어떤 차이점이 있는지 분석해본다.
     * - 로그가 찍힌 시간
     * - 스레드명(nio-8080-exec-x)으로 생성된 스레드 갯수를 파악
     * - http call count
     * - 테스트 결과값
     */
    /**
     * server:
     *   tomcat:
     *     accept-count: 1
     *     max-connections: 1
     *     threads:
     *       max: 2 일 때,
     * 2022-09-07 01:14:12.885  INFO 2784 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 1
     * 2022-09-07 01:14:14.519  INFO 2784 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 2
     * 2022-09-07 01:14:15.037  INFO 2784 --- [nio-8080-exec-2] concurrency.stage2.SampleController      : http call count : 3
     * 2022-09-07 01:14:15.567  INFO 2784 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 4
     * 2022-09-07 01:14:16.104  INFO 2784 --- [nio-8080-exec-2] concurrency.stage2.SampleController      : http call count : 5
     *
     * 2022-09-07 01:20:31.329  INFO 3052 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 1
     * 2022-09-07 01:20:32.917  INFO 3052 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 2
     * 2022-09-07 01:20:33.441  INFO 3052 --- [nio-8080-exec-2] concurrency.stage2.SampleController      : http call count : 3
     * 2022-09-07 01:20:33.983  INFO 3052 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 4
     *
     * server:
     *   tomcat:
     *     accept-count: 1
     *     max-connections: 1
     *     threads:
     *       max: 5 일때,
     *2022-09-07 01:19:31.212  INFO 8088 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 1
     * 2022-09-07 01:19:32.797  INFO 8088 --- [nio-8080-exec-3] concurrency.stage2.SampleController      : http call count : 2
     *
     * server:
     *   tomcat:
     *     accept-count: 2
     *     max-connections: 1
     *     threads:
     *       max: 2
     *
     *       2022-09-07 01:21:33.424  INFO 15424 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 1
     * 2022-09-07 01:21:35.314  INFO 15424 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 2
     * 2022-09-07 01:21:35.829  INFO 15424 --- [nio-8080-exec-2] concurrency.stage2.SampleController      : http call count : 3
     * 2022-09-07 01:21:36.346  INFO 15424 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 4
     *
     * server:
     *   tomcat:
     *     accept-count: 1
     *     max-connections: 50
     *     threads:
     *       max: 2
     *
     * 2022-09-07 01:24:20.135  INFO 7052 --- [nio-8080-exec-2] concurrency.stage2.SampleController      : http call count : 12
     * 2022-09-07 01:24:20.135  INFO 7052 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 11
     * 2022-09-07 01:24:20.641  INFO 7052 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 13
     * 2022-09-07 01:24:20.641  INFO 7052 --- [nio-8080-exec-2] concurrency.stage2.SampleController      : http call count : 14
     * 2022-09-07 01:24:21.159  INFO 7052 --- [nio-8080-exec-2] concurrency.stage2.SampleController      : http call count : 16
     * 2022-09-07 01:24:21.159  INFO 7052 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 15
     * 2022-09-07 01:24:21.680  INFO 7052 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 18
     * 2022-09-07 01:24:21.680  INFO 7052 --- [nio-8080-exec-2] concurrency.stage2.SampleController      : http call count : 17
     * 2022-09-07 01:24:22.200  INFO 7052 --- [nio-8080-exec-2] concurrency.stage2.SampleController      : http call count : 19
     * 2022-09-07 01:24:22.200  INFO 7052 --- [nio-8080-exec-1] concurrency.stage2.SampleController      : http call count : 20
     * @throws Exception
     */
    @Test
    void test() throws Exception {
        final var NUMBER_OF_THREAD = 10;
        var threads = new Thread[NUMBER_OF_THREAD];

        for (int i = 0; i < NUMBER_OF_THREAD; i++) {
            threads[i] = new Thread(() -> incrementIfOk(TestHttpUtils.send("/test")));
        }

        for (final var thread : threads) {
            thread.start();
            Thread.sleep(50);
        }

        for (final var thread : threads) {
            thread.join();
        }

        assertThat(count.intValue()).isEqualTo(2);
    }

    private static void incrementIfOk(final HttpResponse<String> response) {
        if (response.statusCode() == 200) {
            count.incrementAndGet();
        }
    }
}
