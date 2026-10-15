package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PulseTracker.class})
class PulseTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life when Pulse Tracker attacks")
    void eachOpponentLosesLifeWhenItAttacks() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new PulseTracker());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Attack trigger loses exactly 1 life before combat damage and does not drain")
    void losesLifeBeforeCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new PulseTracker());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            resolveAllTriggers();
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        });
    }

    @Test
    @DisplayName("Attack trigger affects the opponent of its controller")
    void opponentControlledTrackerLosesLifeForPlayerOne() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player2, new PulseTracker());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        });
    }

    @Test
    @DisplayName("Only attacking Pulse Trackers trigger, once each")
    void eachAttackingTrackerTriggersIndependently() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new PulseTracker());
        addCreatureReady(player1, new PulseTracker());
        addCreatureReady(player1, new PulseTracker());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            assertThat(gd.stack).hasSize(2);
            resolveAllTriggers();
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        });
    }

    @Test
    @DisplayName("Blocking Pulse Tracker does not prevent its attack-trigger life loss")
    void blockedTrackerStillCausesLifeLoss() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new PulseTracker());
        addCreatureReady(player2, new PulseTracker());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
