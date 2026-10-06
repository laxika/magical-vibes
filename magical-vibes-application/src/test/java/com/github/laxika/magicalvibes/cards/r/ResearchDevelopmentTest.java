package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PillarOfTheParuns;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResearchDevelopment.class, PillarOfTheParuns.class, PsychogenicProbe.class})
class ResearchDevelopmentTest extends BaseCardTest {

    @Test
    @DisplayName("Research shuffles up to four chosen outside-the-game cards into the library")
    void researchShufflesUpToFourCards() {
        List<Card> sideboard = List.of(
                new PillarOfTheParuns(), new PillarOfTheParuns(), new PillarOfTheParuns(),
                new PillarOfTheParuns(), new PillarOfTheParuns());
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(sideboard));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ResearchDevelopment()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ShuffleCardsFromOutsideGameChoice.class);
        List<Card> chosen = sideboard.subList(0, 4);
        harness.handleMultipleCardsChosen(player1, chosen.stream().map(Card::getId).toList());

        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(sideboard.get(4));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(chosen);
    }

    @Test
    @DisplayName("Research can shuffle fewer than four chosen outside-the-game cards")
    void researchShufflesOnlyTheCardsChosen() {
        List<Card> sideboard = List.of(
                new PillarOfTheParuns(), new PillarOfTheParuns(), new PillarOfTheParuns());
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(sideboard));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ResearchDevelopment()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(sideboard.get(1).getId()));

        assertThat(gd.playerSideboards.get(player1.getId()))
                .containsExactly(sideboard.get(0), sideboard.get(2));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(sideboard.get(1));
    }

    @Test
    @DisplayName("Development creates three tokens when every opponent declines")
    void developmentCreatesThreeTokensWhenEveryOpponentDeclines() {
        castDevelopment();

        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Elemental")).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Development draws only once when an opponent accepts, and repeats twice more")
    void developmentDrawsOnceAndSuppressesOnlyThatIterationToken() {
        Card firstDraw = new PillarOfTheParuns();
        Card secondDraw = new PillarOfTheParuns();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        castDevelopment();

        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(findPermanents(player1, "Elemental")).hasSize(2);
    }

    @Test
    @DisplayName("Research still shuffles when zero outside-the-game cards are chosen")
    void researchShufflesWhenZeroCardsAreChosen() {
        Card outsideCard = new PillarOfTheParuns();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(outsideCard)));
        harness.setLibrary(player1, List.of(new PillarOfTheParuns()));
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);

        castResearch();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(outsideCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Research still shuffles an empty library when no outside-the-game cards exist")
    void researchShufflesWithoutOutsideGameCards() {
        gd.playerSideboards.put(player1.getId(), new ArrayList<>());
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setLife(player1, 20);

        castResearch();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Development draws three cards when the opponent accepts every iteration")
    void developmentCanDrawThreeCardsWithoutCreatingTokens() {
        List<Card> cards = List.of(new PillarOfTheParuns(), new PillarOfTheParuns(), new PillarOfTheParuns());
        harness.setLibrary(player1, cards);
        castDevelopment();

        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(cards);
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).doesNotContainAnyElementsOf(cards);
    }

    @Test
    @DisplayName("Development creates red 3/1 Elemental creature tokens for its controller")
    void developmentCreatesTheSpecifiedTokens() {
        castDevelopment();
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Elemental")).hasSize(3).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
            assertThat(token.getEffectivePower()).isEqualTo(3);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
        assertThat(findPermanents(player2, "Elemental")).isEmpty();
    }

    private void castResearch() {
        harness.setHand(player1, List.of(new ResearchDevelopment()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
    }

    private void castDevelopment() {
        harness.setHand(player1, List.of(new ResearchDevelopment()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();
    }
}
