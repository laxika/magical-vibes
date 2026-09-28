package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TakeTheBait.class, GrizzlyBears.class, ChandraNalaar.class})
class TakeTheBaitTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage, untaps and goads attackers, and creates another combat")
    void preventsDamageUntapsAndGoadsAttackers() {
        Permanent attackerToPlayer = addCreatureReady(player1, new GrizzlyBears());
        Permanent attackerToPlaneswalker = addCreatureReady(player1, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1), Map.of(
                0, player2.getId(),
                1, planeswalker.getId()));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new TakeTheBait(), "{2}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(attackerToPlayer.isTapped()).isFalse();
        assertThat(attackerToPlaneswalker.isTapped()).isFalse();
        assertThat(gqs.isGoaded(gd, attackerToPlayer)).isTrue();
        assertThat(gqs.isGoaded(gd, attackerToPlaneswalker)).isTrue();

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
    }

    @Test
    @DisplayName("Can only be cast during an opponent's combat")
    void cannotBeCastDuringYourOwnTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TakeTheBait()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 3);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
