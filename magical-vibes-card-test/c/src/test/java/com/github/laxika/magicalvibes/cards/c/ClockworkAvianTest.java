package com.github.laxika.magicalvibes.cards.c;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ClockworkAvian.class)
class ClockworkAvianTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+0 counters, making it a 4/4")
    void entersWithFourCounters() {
        harness.setHand(player1, List.of(new ClockworkAvian()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent avian = findPermanent(player1, "Clockwork Avian");
        assertThat(avian.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, avian)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, avian)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking removes a +1/+0 counter at end of combat")
    void attackingRemovesCounterAtEndOfCombat() {
        Permanent avian = addCreatureReady(player1, new ClockworkAvian());
        avian.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 4);

        declareAttackers(player1, List.of(0));
        // No blockers exist, so priorities cascade through combat damage and out of end of combat,
        // draining the scheduled counter removal.
        harness.passBothPriorities();

        assertThat(avian.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, avian)).isEqualTo(3);
    }

    @Test
    void blockingRemovesCounterAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new ClockworkAvian());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 0);
        attacker.setAttacking(true);

        Permanent avian = addCreatureReady(player2, new ClockworkAvian());
        avian.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        resolveAllTriggers();

        assertThat(avian.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isZero();
    }

    @Test
    void canAttackWithNoCounters() {
        Permanent avian = addCreatureReady(player1, new ClockworkAvian());
        avian.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 0);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(avian.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(avian);
    }

    @Test
    @DisplayName("No counter is removed when it neither attacks nor blocks")
    void noCounterRemovedWhenNotInCombat() {
        Permanent avian = addCreatureReady(player1, new ClockworkAvian());
        avian.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 4);

        declareAttackers(player1, List.of()); // stays back
        harness.passBothPriorities();
        leaveEndOfCombat();

        assertThat(avian.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(4);
    }

    @Test
    @DisplayName("Upkeep ability adds X +1/+0 counters below the cap")
    void upkeepAbilityAddsCountersBelowCap() {
        Permanent avian = addCreatureReady(player1, new ClockworkAvian());
        avian.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 1);

        activateUpkeepAbility(2);

        assertThat(avian.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(3);
        assertThat(avian.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Upkeep ability cannot raise the total above four")
    void upkeepAbilityCappedAtFour() {
        Permanent avian = addCreatureReady(player1, new ClockworkAvian());
        avian.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 2);

        activateUpkeepAbility(5); // would be 7, but capped at 4

        assertThat(avian.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(4);
    }

    @Test
    @DisplayName("Upkeep ability cannot be activated outside your upkeep")
    void cannotActivateOutsideUpkeep() {
        addCreatureReady(player1, new ClockworkAvian());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    void attackingDoesNotTriggerUntilEndOfCombat() {
        Permanent avian = addCreatureReady(player1, new ClockworkAvian());
        avian.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 4);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(avian.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(4);
    }

    @Test
    void endOfCombatCounterRemovalUsesTheStack() {
        Permanent avian = addCreatureReady(player1, new ClockworkAvian());
        avian.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 4);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            declareAttackers(player1, List.of(0));
            harness.passUntil(TurnStep.END_OF_COMBAT);
        });

        assertThat(avian.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(4);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(avian.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(3);
    }

    @Test
    void mayChooseNoCountersForPositiveX() {
        Permanent avian = addCreatureReady(player1, new ClockworkAvian());
        avian.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 1);
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "0");

        assertThat(avian.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(1);
        assertThat(avian.isTapped()).isTrue();
    }

    @Test
    void cannotActivateDuringOpponentsUpkeep() {
        addCreatureReady(player1, new ClockworkAvian());
        advanceToUpkeep(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    private void activateUpkeepAbility(int x) {
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, x);

        harness.activateAbility(player1, 0, x, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "2");
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
