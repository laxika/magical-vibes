package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaithlessSalvaging.class, OrnithopterOfParadise.class})
class FaithlessSalvagingTest extends BaseCardTest {

    @Test
    void discardsThenDrawsAndRebounds() {
        OrnithopterOfParadise drawnCard = new OrnithopterOfParadise();
        harness.setLibrary(player1, List.of(drawnCard));
        FaithlessSalvaging card = new FaithlessSalvaging();
        OrnithopterOfParadise discardedCard = new OrnithopterOfParadise();
        harness.setHand(player1, List.of(card, discardedCard));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundCanBeCastForFreeAtNextUpkeep() {
        OrnithopterOfParadise firstDraw = new OrnithopterOfParadise();
        OrnithopterOfParadise secondDraw = new OrnithopterOfParadise();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        FaithlessSalvaging card = new FaithlessSalvaging();
        harness.setHand(player1, List.of(card, new OrnithopterOfParadise(), new OrnithopterOfParadise()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
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
        harness.setLibrary(player1, List.of(new OrnithopterOfParadise()));
        FaithlessSalvaging card = new FaithlessSalvaging();
        harness.setHand(player1, List.of(card, new OrnithopterOfParadise()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void drawsEvenWhenThereIsNoCardToDiscard() {
        FaithlessSalvaging card = new FaithlessSalvaging();
        OrnithopterOfParadise drawnCard = new OrnithopterOfParadise();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, card, "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void waitsForDiscardBeforeDrawingAndExiling() {
        FaithlessSalvaging card = new FaithlessSalvaging();
        OrnithopterOfParadise discardedCard = new OrnithopterOfParadise();
        OrnithopterOfParadise drawnCard = new OrnithopterOfParadise();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(card, discardedCard));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discardedCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void reboundWaitsForControllersUpkeepAndGoesToGraveyardAfterRecasting() {
        FaithlessSalvaging card = new FaithlessSalvaging();
        OrnithopterOfParadise firstDraw = new OrnithopterOfParadise();
        OrnithopterOfParadise secondDraw = new OrnithopterOfParadise();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.castFromHand(player1, card, "{1}{R}");
        harness.passBothPriorities();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstDraw, card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }
}
