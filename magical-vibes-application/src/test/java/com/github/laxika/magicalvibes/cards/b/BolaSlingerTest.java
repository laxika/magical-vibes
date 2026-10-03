package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BolaSlinger.class, FountainOfYouth.class, GrizzlyBears.class})
class BolaSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("Backup puts a +1/+1 counter on another creature and grants its attack trigger")
    void backsUpAnotherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bola = castBolaSlinger();

        resolveEtbTargeting(bears);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bola.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Backup targeting the source puts the counter on it and preserves its native attack trigger")
    void backingUpSourceDoesNotGrantAbility() {
        Permanent bola = castBolaSlinger();
        resolveEtbTargeting(bola);

        assertThat(bola.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        bola.setSummoningSick(false);
        declareAttack(bola);

        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();

        assertThat(fountain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted attack trigger taps only an opponent's artifact")
    void attackTriggerTapsOnlyOpponentArtifact() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bola = castBolaSlinger();
        resolveEtbTargeting(bears);

        Permanent ownFountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent opponentFountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        bears.setSummoningSick(false);
        declareAttack(bears);
        harness.handlePermanentChosen(player1, opponentFountain.getId());
        harness.passBothPriorities();

        assertThat(ownFountain.isTapped()).isFalse();
        assertThat(opponentFountain.isTapped()).isTrue();
        assertThat(bola.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The granted attack trigger expires at end of turn")
    void attackTriggerExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBolaSlinger();
        resolveEtbTargeting(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        bears.setSummoningSick(false);
        declareAttack(bears);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(fountain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Bola Slinger keeps its own attack trigger when backup targets another creature")
    void nativeAttackTriggerRemainsWhenBackingUpAnotherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bola = castBolaSlinger();
        resolveEtbTargeting(bears);

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bola.setSummoningSick(false);
        declareAttack(bola);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted attack trigger can tap an opponent's creature")
    void grantedAttackTriggerTapsOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castBolaSlinger();
        resolveEtbTargeting(bears);

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);
        declareAttack(bears);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Backup can target an opponent's creature and grants an ability controlled by that opponent")
    void backsUpOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castBolaSlinger();
        resolveEtbTargeting(opponentCreature);

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        opponentCreature.setSummoningSick(false);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(opponentCreature);
        declareAttackers(player2, List.of(attackerIndex));
        harness.handlePermanentChosen(player2, fountain.getId());
        harness.passBothPriorities();

        assertThat(fountain.isTapped()).isTrue();
    }

    private Permanent castBolaSlinger() {
        harness.setHand(player1, List.of(new BolaSlinger()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Bola Slinger");
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void declareAttack(Permanent attacker) {
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackers(player1, List.of(attackerIndex));
    }
}
