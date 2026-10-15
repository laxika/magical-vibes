package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrightPalmSoulAwakener.class, GrizzlyBears.class})
class BrightPalmSoulAwakenerTest extends BaseCardTest {

    @Test
    @DisplayName("Backup grants the attack trigger, which doubles counters and restricts blockers")
    void backsUpAnotherCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent brightPalm = castBrightPalm();
        resolveEtbTargeting(attacker);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        prepareDeclareBlockers(player1);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);

        assertThat(brightPalm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Backup targeting Bright-Palm only puts a counter on it")
    void backsUpItself() {
        Permanent brightPalm = castBrightPalm();
        resolveEtbTargeting(brightPalm);

        assertThat(brightPalm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        brightPalm.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(brightPalm)));
        harness.handlePermanentChosen(player1, brightPalm.getId());
        harness.passBothPriorities();

        assertThat(brightPalm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("Bright-Palm can double counters on an opponent's nonattacking creature")
    void intrinsicAttackTriggerTargetsOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent brightPalm = castBrightPalm();
        resolveEtbTargeting(target);
        brightPalm.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(brightPalm)));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(brightPalm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A target with no counters still receives the blocking restriction")
    void zeroCountersStillRestrictsBlockers() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent brightPalm = castBrightPalm();
        resolveEtbTargeting(brightPalm);
        brightPalm.setSummoningSick(false);

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(brightPalm)));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Bright-Palm receiving backup has two independently targeted attack triggers")
    void backupAddsAnIndependentAttackTrigger() {
        Permanent opponentBrightPalm = addCreatureReady(player2, new BrightPalmSoulAwakener());
        Permanent ownBrightPalm = castBrightPalm();
        resolveEtbTargeting(opponentBrightPalm);

        declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(opponentBrightPalm)));
        harness.handlePermanentChosen(player2, opponentBrightPalm.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player2, ownBrightPalm.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentBrightPalm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ownBrightPalm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Backup's granted attack trigger expires at the end of the turn")
    void grantedAttackTriggerExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castBrightPalm();
        resolveEtbTargeting(attacker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent castBrightPalm() {
        harness.castFromHand(player1, new BrightPalmSoulAwakener(), "{1}{R}{G}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Bright-Palm, Soul Awakener");
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
