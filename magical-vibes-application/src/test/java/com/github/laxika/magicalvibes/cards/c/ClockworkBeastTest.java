package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClockworkBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with seven +1/+0 counters, making it a 7/4")
    void entersWithSevenCounters() {
        harness.setHand(player1, List.of(new ClockworkBeast()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent beast = findPermanent(player1, "Clockwork Beast");
        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking removes a +1/+0 counter at end of combat")
    void attackingRemovesCounterAtEndOfCombat() {
        Permanent beast = addCreatureReady(player1, new ClockworkBeast());
        beast.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 7);

        declareAttackers(player1, List.of(0));
        // No blockers exist, so priorities cascade through combat damage and out of end of combat,
        // draining the scheduled counter removal.
        harness.passBothPriorities();

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(6);
    }

    @Test
    @DisplayName("No counter is removed when it neither attacks nor blocks")
    void noCounterRemovedWhenNotInCombat() {
        Permanent beast = addCreatureReady(player1, new ClockworkBeast());
        beast.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 7);

        declareAttackers(player1, List.of()); // stays back
        harness.passBothPriorities();
        leaveEndOfCombat();

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(7);
    }

    @Test
    @DisplayName("Upkeep ability adds X +1/+0 counters below the cap")
    void upkeepAbilityAddsCountersBelowCap() {
        Permanent beast = addCreatureReady(player1, new ClockworkBeast());
        beast.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 2);

        activateUpkeepAbility(3);

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(5);
        assertThat(beast.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Upkeep ability cannot raise the total above seven")
    void upkeepAbilityCappedAtSeven() {
        Permanent beast = addCreatureReady(player1, new ClockworkBeast());
        beast.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 4);

        activateUpkeepAbility(5); // would be 9, but capped at 7

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(7);
    }

    @Test
    @DisplayName("Upkeep ability cannot be activated outside your upkeep")
    void cannotActivateOutsideUpkeep() {
        addCreatureReady(player1, new ClockworkBeast());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    private void activateUpkeepAbility(int x) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, x);

        harness.activateAbility(player1, 0, x, null);
        harness.passBothPriorities();
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
