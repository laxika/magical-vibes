package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClockworkSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+0 counters, making it a 4/3")
    void entersWithFourCounters() {
        harness.setHand(player1, List.of(new ClockworkSwarm()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent swarm = findPermanent(player1, "Clockwork Swarm");
        assertThat(swarm.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, swarm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking removes a +1/+0 counter at end of combat")
    void attackingRemovesCounterAtEndOfCombat() {
        Permanent swarm = addCreatureReady(player1, new ClockworkSwarm());
        swarm.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 4);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(swarm.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, swarm)).isEqualTo(3);
    }

    @Test
    @DisplayName("No counter is removed when it neither attacks nor blocks")
    void noCounterRemovedWhenNotInCombat() {
        Permanent swarm = addCreatureReady(player1, new ClockworkSwarm());
        swarm.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 4);

        declareAttackers(player1, List.of()); // stays back
        harness.passBothPriorities();
        leaveEndOfCombat();

        assertThat(swarm.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(4);
    }

    @Test
    @DisplayName("Upkeep ability adds X +1/+0 counters below the cap")
    void upkeepAbilityAddsCountersBelowCap() {
        Permanent swarm = addCreatureReady(player1, new ClockworkSwarm());
        swarm.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 1);

        activateUpkeepAbility(2);

        assertThat(swarm.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(3);
        assertThat(swarm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Upkeep ability cannot raise the total above four")
    void upkeepAbilityCappedAtFour() {
        Permanent swarm = addCreatureReady(player1, new ClockworkSwarm());
        swarm.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, 2);

        activateUpkeepAbility(5); // would be 7, but capped at 4

        assertThat(swarm.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isEqualTo(4);
    }

    @Test
    @DisplayName("Upkeep ability cannot be activated outside your upkeep")
    void cannotActivateOutsideUpkeep() {
        addCreatureReady(player1, new ClockworkSwarm());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot be blocked by a Wall")
    void cannotBeBlockedByWall() {
        setUpAttackWithBlocker(new WallOfWood());

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Can be blocked by a non-Wall creature")
    void canBeBlockedByNonWall() {
        Permanent blocker = setUpAttackWithBlocker(new GrizzlyBears());

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent setUpAttackWithBlocker(Card blockerCard) {
        Permanent swarm = new Permanent(new ClockworkSwarm());
        swarm.setSummoningSick(false);
        swarm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(swarm);

        Permanent blocker = new Permanent(blockerCard);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        return blocker;
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
