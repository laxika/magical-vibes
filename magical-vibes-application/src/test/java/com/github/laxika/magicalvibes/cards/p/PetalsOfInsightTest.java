package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PetalsOfInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers the choice between bottoming the three cards and drawing them")
    void resolvingOffersChoice() {
        castPetals();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining draws three cards and Petals of Insight goes to the graveyard")
    void decliningDrawsThreeCards() {
        castPetals();

        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Petals of Insight");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting bottoms the three cards in a chosen order and returns Petals of Insight to hand")
    void acceptingBottomsCardsAndReturnsSpell() {
        castPetals();

        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(5);
        assertThat(deck.subList(0, 2)).extracting(Card::getName)
                .containsExactly("Mountain", "Mountain");
        assertThat(deck.subList(2, 5)).extracting(Card::getName)
                .containsExactlyInAnyOrder("Llanowar Elves", "Shock", "Llanowar Elves");

        harness.assertInHand(player1, "Petals of Insight");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    /** Sets a known five-card library and resolves Petals of Insight up to its may-choice. */
    private void castPetals() {
        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new LlanowarElves(), new Shock(), new LlanowarElves(),
                new Mountain(), new Mountain()));

        harness.setHand(player1, List.of(new PetalsOfInsight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
