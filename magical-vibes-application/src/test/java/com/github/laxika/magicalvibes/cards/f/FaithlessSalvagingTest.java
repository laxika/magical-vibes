package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaithlessSalvaging.class, GrizzlyBears.class, Island.class})
class FaithlessSalvagingTest extends BaseCardTest {

    @Test
    void discardsThenDrawsAndRebounds() {
        Island drawnCard = new Island();
        setDeck(List.of(drawnCard));
        FaithlessSalvaging card = new FaithlessSalvaging();
        GrizzlyBears discardedCard = new GrizzlyBears();
        harness.setHand(player1, List.of(card, discardedCard));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundCanBeCastForFreeAtNextUpkeep() {
        Island firstDraw = new Island();
        Island secondDraw = new Island();
        setDeck(List.of(firstDraw, secondDraw));
        FaithlessSalvaging card = new FaithlessSalvaging();
        harness.setHand(player1, List.of(card, new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void decliningReboundLeavesTheCardExiled() {
        setDeck(List.of(new Island()));
        FaithlessSalvaging card = new FaithlessSalvaging();
        harness.setHand(player1, List.of(card, new GrizzlyBears()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    private void setDeck(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
