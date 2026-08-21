package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WinternightStories.class, GrizzlyBears.class, Island.class})
class WinternightStoriesTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards, then discarding a creature only discards one card")
    void discardingCreatureUsesTheOneCardOption() {
        castStories(List.of(new GrizzlyBears(), new Island(), new Island()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        List<Card> hand = gd.playerHands.get(player1.getId());
        int creatureIndex = hand.stream().filter(card -> card.hasType(CardType.CREATURE))
                .map(hand::indexOf).findFirst().orElseThrow();
        assertThat(discard.validIndices()).containsExactly(creatureIndex);
        assertThat(discard.remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, creatureIndex);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Winternight Stories");
    }

    @Test
    @DisplayName("May still discard two cards when a creature card is available")
    void decliningCreatureOptionDiscardsTwo() {
        castStories(List.of(new GrizzlyBears(), new Island(), new Island()));

        harness.handleMayAbilityChosen(player1, false);

        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard.validIndices()).containsExactly(0, 1, 2);
        assertThat(discard.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Without a creature card, discarding two is mandatory")
    void noCreatureForcesDiscardTwo() {
        castStories(List.of(new Island(), new Island(), new Island()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void harmonizeCastsFromGraveyardAndExilesTheSpell() {
        Card drawnCard = new Island();
        Card spell = new WinternightStories();
        harness.setLibrary(player1, List.of(drawnCard, new Island(), new Island()));
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashbackWithTapCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void harmonizeReducesGenericCostByTappedCreaturePower() {
        Card drawnCard = new Island();
        Card spell = new WinternightStories();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(drawnCard, new Island(), new Island()));
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashbackWithTapCost(player1, 0, List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    private void castStories(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new WinternightStories()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
