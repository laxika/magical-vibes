package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AnabaBodyguard;
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

@CardUsed({ClockworkSteed.class, ClockworkGnomes.class, AnabaBodyguard.class})
class ClockworkSteedTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+0 counters, making it a 4/3")
    void entersWithFourCounters() {
        harness.setHand(player1, List.of(new ClockworkSteed()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent steed = findPermanent(player1, "Clockwork Steed");
        assertThat(steed.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking removes a +1/+0 counter at end of combat")
    void attackingRemovesCounterAtEndOfCombat() {
        Permanent steed = addCreatureReady(player1, new ClockworkSteed());
        steed.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 4);

        declareAttackers(List.of(0));
        assertThat(steed.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(steed.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(3);
    }

    @Test
    @DisplayName("No counter is removed when it neither attacks nor blocks")
    void noCounterRemovedWhenNotInCombat() {
        Permanent steed = addCreatureReady(player1, new ClockworkSteed());
        steed.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 4);

        declareAttackers(List.of()); // stays back
        harness.passBothPriorities();
        leaveEndOfCombat();

        assertThat(steed.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(4);
    }

    @Test
    @DisplayName("Blocking removes a +1/+0 counter at end of combat")
    void blockingRemovesCounterAtEndOfCombat() {
        addCreatureReady(player1, new AnabaBodyguard());
        Permanent steed = addCreatureReady(player2, new ClockworkSteed());
        steed.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 4);

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(steed.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(4);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(steed.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(3);
    }

    @Test
    @DisplayName("Upkeep ability adds X +1/+0 counters below the cap")
    void upkeepAbilityAddsCountersBelowCap() {
        Permanent steed = addCreatureReady(player1, new ClockworkSteed());
        steed.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 1);

        activateUpkeepAbility(2);

        assertThat(steed.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(3);
        assertThat(steed.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Upkeep ability cannot raise the total above four")
    void upkeepAbilityCappedAtFour() {
        Permanent steed = addCreatureReady(player1, new ClockworkSteed());
        steed.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 2);

        activateUpkeepAbility(5); // would be 7, but capped at 4

        assertThat(steed.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(4);
    }

    @Test
    @DisplayName("Upkeep ability cannot be activated outside your upkeep")
    void cannotActivateOutsideUpkeep() {
        addCreatureReady(player1, new ClockworkSteed());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Upkeep ability cannot be activated during an opponent's upkeep")
    void cannotActivateDuringOpponentsUpkeep() {
        addCreatureReady(player1, new ClockworkSteed());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot be blocked by an artifact creature")
    void cannotBeBlockedByArtifactCreature() {
        Permanent steed = new Permanent(new ClockworkSteed());
        steed.setSummoningSick(false);
        steed.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(steed);

        Permanent blocker = addCreatureReady(player2, new ClockworkGnomes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Can be blocked by a non-artifact creature")
    void canBeBlockedByNonArtifactCreature() {
        Permanent steed = new Permanent(new ClockworkSteed());
        steed.setSummoningSick(false);
        steed.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(steed);

        Permanent blocker = addCreatureReady(player2, new AnabaBodyguard());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void activateUpkeepAbility(int x) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, x);

        harness.activateAbility(player1, 0, x, null);
        harness.passBothPriorities();
    }

    private void leaveEndOfCombat() {
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
