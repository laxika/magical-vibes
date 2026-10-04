package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GristleGlutton.class})
class GristleGluttonTest extends BaseCardTest {

    @Test
    void blightsCreatureThenDiscardingDrawsACard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent glutton = harness.addToBattlefieldAndReturn(player1, new GristleGlutton());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GristleGlutton());
        glutton.setSummoningSick(false);

        Card discardedCard = new GristleGlutton();
        Card drawnCard = new GristleGlutton();
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GristleGlutton());
        glutton.setSummoningSick(false);
        harness.setHand(player1, List.of());
        Card undrawnCard = new GristleGlutton();
        harness.setLibrary(player1, List.of(undrawnCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawnCard);
        assertThat(glutton.isTapped()).isTrue();
    }

    @Test
    void blightIsPaidBeforeAbilityResolvesAndCanBlightItself() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent glutton = harness.addToBattlefieldAndReturn(player1, new GristleGlutton());
        glutton.setSummoningSick(false);
        Card heldCard = new GristleGlutton();
        harness.setHand(player1, List.of(heldCard));

        harness.activateAbility(player1, 0, 0, null, null);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, glutton.getId());
        }

        assertThat(glutton.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(glutton.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(heldCard);
    }
}
