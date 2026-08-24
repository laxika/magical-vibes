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
    @DisplayName("Backup targeting the source still puts the counter on it without granting the attack trigger")
    void backingUpSourceDoesNotGrantAbility() {
        Permanent bola = castBolaSlinger();
        resolveEtbTargeting(bola);

        assertThat(bola.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        bola.setSummoningSick(false);
        declareAttack(bola);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(fountain.isTapped()).isFalse();
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

    private Permanent castBolaSlinger() {
        harness.setHand(player1, List.of(new BolaSlinger()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BolaSlinger)
                .findFirst()
                .orElseThrow();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void declareAttack(Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        harness.getGameService().declareAttackers(gd, player1, List.of(attackerIndex));
    }
}
