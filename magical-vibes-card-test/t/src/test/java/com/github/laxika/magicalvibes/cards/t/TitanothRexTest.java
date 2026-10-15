package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TitanothRex.class)
class TitanothRexTest extends BaseCardTest {

    @Test
    void cyclingPutsTrampleCounterOnOwnCreatureBeforeDrawing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TitanothRex());
        prepareCycling();

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Titanoth Rex");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        harness.assertNotInHand(player1, "Titanoth Rex");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Titanoth Rex");
    }

    @Test
    void counterTriggerCannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TitanothRex());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new TitanothRex());
        prepareCycling();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        harness.assertInHand(player1, "Titanoth Rex");
    }

    @Test
    void cyclingWithoutLegalCounterTargetStillDraws() {
        prepareCycling();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Titanoth Rex");
        harness.assertInHand(player1, "Titanoth Rex");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingCounterTargetDoesNotPreventCyclingDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TitanothRex());
        prepareCycling();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Titanoth Rex");
        assertThat(gd.stack).isEmpty();
    }

    private void prepareCycling() {
        harness.setHand(player1, List.of(new TitanothRex()));
        harness.setLibrary(player1, List.of(new TitanothRex()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}