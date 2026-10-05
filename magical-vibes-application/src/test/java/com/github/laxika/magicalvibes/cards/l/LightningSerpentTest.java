package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningSerpent.class})
class LightningSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack immediately because of haste")
    void canAttackImmediatelyBecauseOfHaste() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new LightningSerpent());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Casting with X=3 enters with three +1/+0 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new LightningSerpent()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent serpent = findPermanent(player1, "Lightning Serpent");
        assertThat(serpent.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, serpent)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting with X=0 enters without +1/+0 counters")
    void entersWithNoCountersAtXZero() {
        harness.setHand(player1, List.of(new LightningSerpent()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent serpent = findPermanent(player1, "Lightning Serpent");
        assertThat(serpent.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isZero();
        assertThat(gqs.getEffectivePower(gd, serpent)).isEqualTo(2);
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningSerpent()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new LightningSerpent());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player2, "Lightning Serpent");
    }

    @Test
    @DisplayName("Sacrifices itself at the end step")
    void sacrificesItselfAtEndStep() {
        harness.addToBattlefield(player1, new LightningSerpent());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lightning Serpent");
        harness.assertInGraveyard(player1, "Lightning Serpent");
    }

    @Test
    @DisplayName("Sacrifices itself at the beginning of an opponent's end step")
    void sacrificesItselfAtBeginningOfOpponentsEndStep() {
        harness.addToBattlefield(player1, new LightningSerpent());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lightning Serpent");
        harness.assertInGraveyard(player1, "Lightning Serpent");
    }

    @Test
    @DisplayName("End-step sacrifice uses the stack and waits for resolution")
    void endStepSacrificeWaitsForResolution() {
        harness.addToBattlefield(player1, new LightningSerpent());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Lightning Serpent");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Lightning Serpent");
        harness.assertInGraveyard(player1, "Lightning Serpent");
    }
}
