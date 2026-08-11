package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GristleGluttonTest extends BaseCardTest {

    @Test
    void blightsCreatureThenDiscardingDrawsACard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent glutton = harness.addToBattlefieldAndReturn(player1, new GristleGlutton());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        glutton.setSummoningSick(false);

        Card discardedCard = new SerraAngel();
        Card drawnCard = new SerraAngel();
        harness.setHand(player1, List.of(discardedCard));
        gd.playerDecks.get(player1.getId()).addFirst(drawnCard);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(glutton.isTapped()).isTrue();
    }

    @Test
    void blightsCreatureButDoesNotDrawWhenNoCardCanBeDiscarded() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent glutton = harness.addToBattlefieldAndReturn(player1, new GristleGlutton());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        glutton.setSummoningSick(false);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(glutton.isTapped()).isTrue();
    }
}
