package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BagOfDevouring.class, GrizzlyBears.class})
class BagOfDevouringTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another nontoken creature exiles it with the Bag and draws a card")
    void sacrificesCreatureExilesItAndDraws() {
        Permanent bag = harness.addToBattlefieldAndReturn(player1, new BagOfDevouring());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card drawn = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(drawn);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(creature.getCard().getId()).sourcePermanentId()).isEqualTo(bag.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(drawn.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bag);
    }

    @Test
    @DisplayName("Sacrificing the Bag rolls a d10 and returns up to that many tracked cards")
    void sacrificeBagReturnsChosenTrackedCards() {
        Permanent bag = harness.addToBattlefieldAndReturn(player1, new BagOfDevouring());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        bag.untap();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(sacrificed.getCard().getId()));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(sacrificed.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(bag.getCard().getId()));
        assertThat(gd.getCardsExiledByPermanent(bag.getId())).isEmpty();
    }
}
