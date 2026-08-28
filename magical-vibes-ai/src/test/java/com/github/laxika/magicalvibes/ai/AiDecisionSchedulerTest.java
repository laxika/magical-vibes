package com.github.laxika.magicalvibes.ai;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AiDecisionSchedulerTest {

    @Test
    void reportsPendingWorkWhileDecisionIsRunning() throws Exception {
        AiDecisionEngine engine = mock(AiDecisionEngine.class);
        CountDownLatch decisionStarted = new CountDownLatch(1);
        CountDownLatch releaseDecision = new CountDownLatch(1);
        doAnswer(invocation -> {
            decisionStarted.countDown();
            assertThat(releaseDecision.await(2, TimeUnit.SECONDS)).isTrue();
            return null;
        }).when(engine).handleEvent(AiDecisionKind.GAME_STATE);

        AiDecisionScheduler scheduler = new AiDecisionScheduler("test", engine, 0);
        assertThat(scheduler.hasPendingWork()).isFalse();

        scheduler.scheduleDecision(AiDecisionKind.GAME_STATE);
        assertThat(decisionStarted.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(scheduler.hasPendingWork()).isTrue();

        releaseDecision.countDown();
        verify(engine, timeout(1_000)).handleEvent(AiDecisionKind.GAME_STATE);
        Thread.sleep(50);
        assertThat(scheduler.hasPendingWork()).isFalse();
        scheduler.close();
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
        }).when(engine).handleEvent(AiDecisionKind.GAME_STATE);

        AiDecisionScheduler scheduler = new AiDecisionScheduler("test", engine, 0);
        scheduler.scheduleDecision(AiDecisionKind.GAME_STATE);
        assertThat(firstDecisionStarted.await(1, TimeUnit.SECONDS)).isTrue();

        for (int i = 0; i < 100; i++) {
            scheduler.scheduleDecision(AiDecisionKind.GAME_STATE);
        }
        releaseFirstDecision.countDown();

        verify(engine, timeout(1_000).times(2))
                .handleEvent(AiDecisionKind.GAME_STATE);
        Thread.sleep(50);
        assertThat(calls).hasValue(2);
        assertThat(scheduler.diagnosticSummary())
                .contains("queuedTasks=0")
                .contains("received=101")
                .contains("handled=2")
                .contains("coalescedGameStates=100");
        scheduler.close();
    }

    @Test
    void queuedDecisionDoesNotRunAfterClose() throws Exception {
        AiDecisionEngine engine = mock(AiDecisionEngine.class);
        AiDecisionScheduler scheduler = new AiDecisionScheduler("test", engine, 100);

        scheduler.scheduleDecision(AiDecisionKind.GAME_STATE);
        scheduler.close();

        Thread.sleep(200);
        verifyNoInteractions(engine);
    }

    @Test
    void combatDecisionFactsWakeTheAiIndependentlyAndInOrder() throws Exception {
        AiDecisionEngine engine = mock(AiDecisionEngine.class);
        CountDownLatch handled = new CountDownLatch(3);
        List<AiDecisionKind> types = Collections.synchronizedList(new ArrayList<>());
        doAnswer(invocation -> {
            types.add(invocation.getArgument(0));
            handled.countDown();
            return null;
        }).when(engine).handleEvent(any(AiDecisionKind.class));

        AiDecisionScheduler scheduler = new AiDecisionScheduler("test", engine, 0);
        scheduler.scheduleDecision(AiDecisionKind.ATTACKER_DECLARATION);
        scheduler.scheduleDecision(AiDecisionKind.BLOCKER_DECLARATION);
        scheduler.scheduleDecision(AiDecisionKind.COMBAT_DAMAGE_ASSIGNMENT);

        assertThat(handled.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(types).containsExactly(
                AiDecisionKind.ATTACKER_DECLARATION,
                AiDecisionKind.BLOCKER_DECLARATION,
                AiDecisionKind.COMBAT_DAMAGE_ASSIGNMENT);
        scheduler.close();
    }

}
