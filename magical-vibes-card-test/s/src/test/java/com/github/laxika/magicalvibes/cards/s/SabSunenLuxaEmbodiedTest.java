package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GuidelightSynergist;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SabSunenLuxaEmbodied.class, GuidelightSynergist.class})
class SabSunenLuxaEmbodiedTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack with an even number of counters, including zero")
    void canAttackWithEvenCounterCount() {
        addCreatureReady(player1, new SabSunenLuxaEmbodied());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack with an odd number of counters")
    void cannotAttackWithOddCounterCount() {
        Permanent sabSunen = addSabSunen(player1);
        sabSunen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot block with an odd number of counters")
    void cannotBlockWithOddCounterCount() {
        addCreatureReady(player2, new GuidelightSynergist());
        Permanent sabSunen = addSabSunen(player1);
        sabSunen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Adds a counter and draws two cards when the new total is odd")
    void addsCounterAndDrawsWhenNewTotalIsOdd() {
        Permanent sabSunen = addSabSunen(player1);
        harness.setLibrary(player1, List.of(new GuidelightSynergist(), new GuidelightSynergist(), new GuidelightSynergist()));

        advanceToPrecombatMain(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(sabSunen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Adds a counter without drawing when the new total is even")
    void addsCounterWithoutDrawingWhenNewTotalIsEven() {
        Permanent sabSunen = addSabSunen(player1);
        sabSunen.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new GuidelightSynergist(), new GuidelightSynergist(), new GuidelightSynergist()));

        advanceToPrecombatMain(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(sabSunen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Does not trigger on an opponent's first main phase")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent sabSunen = addSabSunen(player1);

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(sabSunen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can attack with two counters of different types")
    void canAttackWithMixedEvenCounters() {
        Permanent sabSunen = addCreatureReady(player1, new SabSunenLuxaEmbodied());
        sabSunen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        sabSunen.setCounterCount(CounterType.CHARGE, 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Reach permits blocking a flying attacker with zero counters")
    void canBlockFlyingAttackerWithZeroCounters() {
        addCreatureReady(player2, new GuidelightSynergist());
        addSabSunen(player1);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Sab-Sunen, Luxa Embodied");
        harness.assertInGraveyard(player2, "Guidelight Synergist");
    }

    @Test
    @DisplayName("Draw condition uses counters at resolution rather than at trigger time")
    void checksCountersAtResolution() {
        Permanent sabSunen = addSabSunen(player1);
        harness.setLibrary(player1, List.of(new GuidelightSynergist(), new GuidelightSynergist()));
        advanceToPrecombatMain(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        sabSunen.setCounterCount(CounterType.CHARGE, 1);

        harness.passBothPriorities();

        assertThat(sabSunen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Tramples over a blocker while an even counter total permits attacking")
    void tramplesOverBlocker() {
        addCreatureReady(player1, new SabSunenLuxaEmbodied());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GuidelightSynergist());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Sab-Sunen, Luxa Embodied");
        harness.assertInGraveyard(player2, "Guidelight Synergist");
    }

    @Test
    @DisplayName("Indestructible keeps both copies alive after lethal combat damage")
    void survivesLethalCombatDamage() {
        addCreatureReady(player1, new SabSunenLuxaEmbodied());
        Permanent blocker = addSabSunen(player2);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));

        harness.assertOnBattlefield(player1, "Sab-Sunen, Luxa Embodied");
        harness.assertOnBattlefield(player2, "Sab-Sunen, Luxa Embodied");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Draws using last known odd counter total when the source leaves before resolution")
    void drawsWhenDepartedSourceHadOddCounters() {
        Permanent sabSunen = addSabSunen(player1);
        harness.setLibrary(player1, List.of(new GuidelightSynergist(), new GuidelightSynergist()));
        advanceToPrecombatMain(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        sabSunen.setCounterCount(CounterType.CHARGE, 1);
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, sabSunen);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sab-Sunen, Luxa Embodied");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Does not trigger at the beginning of the second main phase")
    void doesNotTriggerInPostcombatMain() {
        Permanent sabSunen = addSabSunen(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(sabSunen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addSabSunen(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SabSunenLuxaEmbodied());
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
