package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MoonlightGeist;
import com.github.laxika.magicalvibes.cards.t.ThrabenValiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmassTheComponents.class, ThrabenValiant.class, MoonlightGeist.class, Abundance.class})
class AmassTheComponentsTest extends BaseCardTest {

    private List<Card> fiveCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cards.add(i % 2 == 0 ? new ThrabenValiant() : new MoonlightGeist());
        }
        return cards;
    }

    private void castAmassTheComponents(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new AmassTheComponents(), "{3}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws three cards, then asks for one hand card to bottom")
    void drawsThreeThenPrompts() {
        List<Card> library = fiveCards();
        castAmassTheComponents(library);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice -> {
                    assertThat(choice.playerId()).isEqualTo(player1.getId());
                    assertThat(choice.maxCount()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Chosen card goes on the bottom of the library with no top/bottom prompt")
    void putsChosenCardOnBottom() {
        List<Card> library = fiveCards();
        castAmassTheComponents(library);

        Card drawn0 = library.get(0);
        Card drawn1 = library.get(1);
        Card drawn2 = library.get(2);

        harness.handleMultipleCardsChosen(player1, List.of(drawn1.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn0, drawn2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(3), library.get(4), drawn1);
    }

    @Test
    @DisplayName("Can put a card already in hand on the bottom")
    void choosesFromEntireHand() {
        List<Card> library = fiveCards();
        Card alreadyInHand = new ThrabenValiant();
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new AmassTheComponents(), alreadyInHand));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(alreadyInHand.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(3), library.get(4), alreadyInHand);
    }

    @Test
    @DisplayName("Putting a card on the bottom is mandatory")
    void cannotDeclinePuttingCardOnBottom() {
        List<Card> library = fiveCards();
        castAmassTheComponents(library);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));

        harness.handleMultipleCardsChosen(player1, List.of(library.get(0).getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(3), library.get(4), library.get(0));
    }

    @Test
    @DisplayName("Drawing exactly the last three cards allows a card to be returned without losing")
    void returnsCardToEmptyLibraryAfterSuccessfulDraws() {
        List<Card> library = fiveCards().subList(0, 3);
        castAmassTheComponents(library);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.handleMultipleCardsChosen(player1, List.of(library.get(1).getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0), library.get(2));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(1));
    }

    @Test
    @DisplayName("Completes all replacement draw choices before choosing a card to bottom")
    void waitsForReplacementDrawsWithCardAlreadyInHand() {
        List<Card> library = fiveCards();
        Card alreadyInHand = new ThrabenValiant();
        harness.addToBattlefield(player1, new Abundance());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new AmassTheComponents(), alreadyInHand));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(library.get(1).getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(alreadyInHand, library.get(0), library.get(2));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(3), library.get(4), library.get(1));
    }

    @Test
    @DisplayName("Still requires a card to be bottomed after replacement draw choices with no initial hand")
    void returnsCardAfterReplacementDrawsWithInitiallyEmptyHand() {
        List<Card> library = fiveCards();
        harness.addToBattlefield(player1, new Abundance());
        castAmassTheComponents(library);

        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(library.get(1).getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0), library.get(2));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(3), library.get(4), library.get(1));
    }
}
