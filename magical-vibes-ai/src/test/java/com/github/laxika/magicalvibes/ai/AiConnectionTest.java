package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.service.JacksonConfig;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class AiConnectionTest {

    private final ObjectMapper objectMapper = new JacksonConfig().objectMapper();

    @Test
    void ignoresInformationalMessagesBeforeSchedulingThem() throws Exception {
        AiDecisionEngine engine = mock(AiDecisionEngine.class);
        AiConnection connection = new AiConnection("test", engine, objectMapper, 0);

        connection.sendMessage("{\"type\":\"BATTLEFIELD_UPDATED\"}");

        Thread.sleep(50);
        verify(engine, never()).handleMessage(anyString(), anyString());
        assertThat(connection.diagnosticSummary())
                .contains("queuedTasks=0")
                .contains("handled=0")
                .contains("ignored=1");
        connection.close();
    }

    @Test
    void coalescesGameStatesAndRetainsOneFollowUpDecision() throws Exception {
        AiDecisionEngine engine = mock(AiDecisionEngine.class);
        CountDownLatch firstDecisionStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstDecision = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        doAnswer(invocation -> {
            if (calls.incrementAndGet() == 1) {
                firstDecisionStarted.countDown();
                assertThat(releaseFirstDecision.await(2, TimeUnit.SECONDS)).isTrue();
            }
            return null;
        }).when(engine).handleMessage("GAME_STATE", "{\"type\":\"GAME_STATE\"}");

        AiConnection connection = new AiConnection("test", engine, objectMapper, 0);
        connection.sendMessage("{\"type\":\"GAME_STATE\"}");
        assertThat(firstDecisionStarted.await(1, TimeUnit.SECONDS)).isTrue();

        for (int i = 0; i < 100; i++) {
            connection.sendMessage("{\"type\":\"GAME_STATE\"}");
        }
        releaseFirstDecision.countDown();

        verify(engine, timeout(1_000).times(2))
                .handleMessage("GAME_STATE", "{\"type\":\"GAME_STATE\"}");
        Thread.sleep(50);
        assertThat(calls).hasValue(2);
        assertThat(connection.diagnosticSummary())
                .contains("queuedTasks=0")
                .contains("handled=2")
                .contains("coalescedGameStates=100");
        connection.close();
    }

    @Test
    void queuedDecisionDoesNotRunAfterClose() throws Exception {
        AiDecisionEngine engine = mock(AiDecisionEngine.class);
        AiConnection connection = new AiConnection("test", engine, objectMapper, 100);

        connection.sendMessage("{\"type\":\"GAME_STATE\"}");
        connection.close();

        Thread.sleep(200);
        verify(engine, never()).handleMessage(anyString(), anyString());
    }
}
