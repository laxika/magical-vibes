package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.networking.message.ErrorMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignmentNotification;
import com.github.laxika.magicalvibes.networking.message.GameOverMessage;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.message.RevealLibraryTopMessage;
import com.github.laxika.magicalvibes.networking.model.MessageType;
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

class AiConnectionTest {

    @Test
    void ignoresInformationalMessagesBeforeSchedulingThem() throws Exception {
        AiDecisionEngine engine = mock(AiDecisionEngine.class);
        AiConnection connection = new AiConnection("test", engine, 0);

        connection.sendMessage(new ErrorMessage("informational"));

        Thread.sleep(50);
        verifyNoInteractions(engine);
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
        }).when(engine).handleEvent(MessageType.GAME_STATE);

        AiConnection connection = new AiConnection("test", engine, 0);
        GameStateMessage gameState = mock(GameStateMessage.class);
        connection.sendMessage(gameState);
        assertThat(firstDecisionStarted.await(1, TimeUnit.SECONDS)).isTrue();

        for (int i = 0; i < 100; i++) {
            connection.sendMessage(gameState);
        }
        releaseFirstDecision.countDown();

        verify(engine, timeout(1_000).times(2))
                .handleEvent(MessageType.GAME_STATE);
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
        AiConnection connection = new AiConnection("test", engine, 100);

        connection.sendMessage(mock(GameStateMessage.class));
        connection.close();

        Thread.sleep(200);
        verifyNoInteractions(engine);
    }

    @Test
    void gameOverClosesConnectionWithoutSchedulingDecision() throws Exception {
        AiDecisionEngine engine = mock(AiDecisionEngine.class);
        AiConnection connection = new AiConnection("test", engine, 0);

        connection.sendMessage(new GameOverMessage(null, null));

        assertThat(connection.isOpen()).isFalse();
        verifyNoInteractions(engine);
    }

    @Test
    void combatDecisionMessagesWakeTheAiIndependentlyAndInOrder() throws Exception {
        AiDecisionEngine engine = mock(AiDecisionEngine.class);
        CountDownLatch handled = new CountDownLatch(3);
        List<MessageType> types = Collections.synchronizedList(new ArrayList<>());
        doAnswer(invocation -> {
            types.add(invocation.getArgument(0));
            handled.countDown();
            return null;
        }).when(engine).handleEvent(any(MessageType.class));

        AiConnection connection = new AiConnection("test", engine, 0);
        connection.sendMessage(mock(AvailableAttackersMessage.class));
        connection.sendMessage(mock(AvailableBlockersMessage.class));
        connection.sendMessage(mock(CombatDamageAssignmentNotification.class));

        assertThat(handled.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(types).containsExactly(
                MessageType.AVAILABLE_ATTACKERS,
                MessageType.AVAILABLE_BLOCKERS,
                MessageType.COMBAT_DAMAGE_ASSIGNMENT);
        connection.close();
    }

    @Test
    void restrictedRevealMessagesAreInformationalForAiRecipients() throws Exception {
        AiDecisionEngine engine = mock(AiDecisionEngine.class);
        AiConnection connection = new AiConnection("test", engine, 0);

        connection.sendMessage(new RevealHandMessage(List.of(), "Opponent"));
        connection.sendMessage(new RevealLibraryTopMessage(List.of(), "Opponent"));

        Thread.sleep(50);
        verifyNoInteractions(engine);
        assertThat(connection.diagnosticSummary())
                .contains("queuedTasks=0")
                .contains("handled=0")
                .contains("ignored=2");
        connection.close();
    }
}
