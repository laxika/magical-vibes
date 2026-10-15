package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChompingKavu.class, GrizzlyBears.class, HillGiant.class, TurnToFrog.class})
class ChompingKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a +1/+1 counter on another creature and restricts its blockers")
    void backsUpAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castChompingKavu();
        resolveEtbTargeting(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        target.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(target)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Backup allows another creature to be blocked by a creature with power 3 or greater")
    void backsUpAnotherCreatureAgainstHighPowerBlocker() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castChompingKavu();
        resolveEtbTargeting(target);

        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(target))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Backup targeting itself preserves Chomping Kavu's printed blocker restriction")
    void backsUpItselfAndRetainsPrintedRestriction() {
        Permanent kavu = castChompingKavu();
        resolveEtbTargeting(kavu);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(kavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        kavu.setSummoningSick(false);
        kavu.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(kavu)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Backup's blocker restriction expires at the end of the turn")
    void grantedRestrictionExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castChompingKavu();
        resolveEtbTargeting(target);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(target))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Chomping Kavu retains its own restriction when backing up another creature")
    void retainsPrintedRestrictionWhenBackingUpAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent kavu = castChompingKavu();
        resolveEtbTargeting(target);

        kavu.setSummoningSick(false);
        kavu.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(kavu)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chomping Kavu can be blocked by a creature with power exactly 3")
    void printedRestrictionAllowsPowerThreeBlocker() {
        Permanent kavu = castChompingKavu();
        resolveEtbTargeting(kavu);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        kavu.setSummoningSick(false);
        kavu.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(kavu))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Backup may target an opponent's creature and grants it the restriction")
    void backsUpOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castChompingKavu();
        resolveEtbTargeting(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        target.setAttacking(true);
        prepareDeclareBlockers(player2);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(target)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted restriction uses the blocker's current power including counters")
    void grantedRestrictionUsesCurrentBlockerPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castChompingKavu();
        resolveEtbTargeting(target);
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(target))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A later loss of all abilities removes the ability granted by backup")
    void grantedRestrictionCanBeRemovedByAbilityLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castChompingKavu();
        resolveEtbTargeting(target);

        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(target))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent castChompingKavu() {
        harness.castFromHand(player1, new ChompingKavu(), "{3}{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Chomping Kavu");
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
