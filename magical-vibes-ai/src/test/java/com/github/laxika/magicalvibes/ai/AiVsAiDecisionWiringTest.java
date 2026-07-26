package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AiVsAiDecisionWiringTest {

    @Test
    void canonicalStateFactWakesBothAisWithoutProducingHumanTransportOutput() throws Exception {
        GameTestHarness harness = new GameTestHarness();
        harness.getGameData().aiPlayerIds.addAll(List.of(
                harness.getPlayer1().getId(),
                harness.getPlayer2().getId()));
        harness.getConn1().clearMessages();
        harness.getConn2().clearMessages();

        AiDecisionScheduler first = mock(AiDecisionScheduler.class);
        AiDecisionScheduler second = mock(AiDecisionScheduler.class);
        AiDecisionEventSubscriber subscriber = new AiDecisionEventSubscriber();
        subscriber.register(harness.getGameData().id, harness.getPlayer1().getId(), first);
        subscriber.register(harness.getGameData().id, harness.getPlayer2().getId(), second);

        try (AutoCloseable ignored = harness.subscribeToGameEvents(subscriber)) {
            harness.publishState();
        }

        verify(first).scheduleDecision(AiDecisionKind.GAME_STATE);
        verify(second).scheduleDecision(AiDecisionKind.GAME_STATE);
        assertThat(harness.getConn1().getSentMessages()).isEmpty();
        assertThat(harness.getConn2().getSentMessages()).isEmpty();
    }
}
