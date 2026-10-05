package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProwlerMisguidedMentor.class, GrizzlyBears.class, HillGiant.class})
class ProwlerMisguidedMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked by a creature with power 2 or less")
    void cannotBeBlockedByLowPowerCreature() {
        Permanent prowler = addAttackingProwler();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(prowler);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Can be blocked by a creature with power 3 or greater")
    void canBeBlockedByHighPowerCreature() {
        Permanent prowler = addAttackingProwler();
        Permanent giant = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(giant);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(prowler);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(giant.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Combat damage puts a +1/+1 counter on another creature you control")
    void combatDamagePutsCounterOnAnotherCreature() {
        Permanent prowler = addAttackingProwler();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId()).doesNotContain(prowler.getId(), opposingBears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Block restriction uses current power rather than printed power")
    void boostedBearsCanBlock() {
        Permanent prowler = addAttackingProwler();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(bears),
                gd.playerBattlefields.get(player1.getId()).indexOf(prowler))));

        assertThat(bears.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Combat damage with no other friendly creature does not prompt for a target")
    void noLegalTargetDoesNotPrompt() {
        Permanent prowler = addAttackingProwler();
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter ability does not retarget when its target leaves the battlefield")
    void removedTargetDoesNotReceiveCounter() {
        Permanent prowler = addAttackingProwler();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        resolveCombat();
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerGraveyards.get(player1.getId()).add(bears.getCard());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not trigger the counter ability")
    void damageToCreatureDoesNotTrigger() {
        Permanent prowler = addAttackingProwler();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(giant),
                gd.playerBattlefields.get(player1.getId()).indexOf(prowler))));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAttackingProwler() {
        Permanent prowler = addCreatureReady(player1, new ProwlerMisguidedMentor());
        prowler.setAttacking(true);
        return prowler;
    }
}
