package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResearchDevelopment.class, GrizzlyBears.class})
class ResearchDevelopmentTest extends BaseCardTest {

    @Test
    @DisplayName("Research shuffles up to four chosen outside-the-game cards into the library")
    void researchShufflesUpToFourCards() {
        List<Card> sideboard = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
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
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        castDevelopment();

        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(findPermanents(player1, "Elemental")).hasSize(2);
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
