package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Island;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Brainstone.class, Island.class})
class BrainstoneTest extends BaseCardTest {

    private List<Card> fiveCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cards.add(new Island());
        }
        return cards;
    }

    private void activateBrainstone(List<Card> library) {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, library);
        harness.addToBattlefield(player1, new Brainstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Sacrifices itself, draws three cards, then asks which two to put on top")
    void sacrificesDrawsThreeThenPrompts() {
        List<Card> library = fiveCards();
        activateBrainstone(library);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.assertInGraveyard(player1, "Brainstone");
    }

    @Test
    @DisplayName("Puts the chosen cards on top in the chosen order")
    void putsChosenCardsOnTop() {
        List<Card> library = fiveCards();
        activateBrainstone(library);

        Card drawn0 = library.get(0);
        Card drawn1 = library.get(1);
        Card drawn2 = library.get(2);
        harness.handleMultipleCardsChosen(player1, List.of(drawn0.getId(), drawn1.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(drawn0, drawn1, library.get(3), library.get(4));
    }

    @Test
    void canPutBackCardsThatWereAlreadyInHandInEitherOrder() {
        List<Card> library = fiveCards();
        Card first = new Island();
        Card second = new Island();
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, library);
        harness.addToBattlefield(player1, new Brainstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(second.getId(), first.getId()));

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(second, first, library.get(3), library.get(4));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotChooseTheSameCardTwice() {
        List<Card> library = fiveCards();
        activateBrainstone(library);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(library.get(0).getId(), library.get(0).getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(3), library.get(4));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
    }

    @Test
    void mustPutBackExactlyTwoCards() {
        List<Card> library = fiveCards();
        activateBrainstone(library);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(library.get(0).getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(library.get(0).getId(), library.get(1).getId(), library.get(2).getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(library.get(2).getId(), library.get(0).getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(1));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(2), library.get(0), library.get(3), library.get(4));
    }

    @Test
    void sacrificeIsPaidBeforeCardsAreDrawn() {
        List<Card> library = fiveCards();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, library);
        harness.addToBattlefield(player1, new Brainstone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Brainstone");
        harness.assertInGraveyard(player1, "Brainstone");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));
    }

    @Test
    void cannotActivateWhileTapped() {
        harness.addToBattlefieldAndReturn(player1, new Brainstone()).tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Brainstone");
        harness.assertNotInGraveyard(player1, "Brainstone");
    }

    @Test
    void cannotActivateWithoutTwoMana() {
        harness.addToBattlefield(player1, new Brainstone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Brainstone");
        harness.assertNotInGraveyard(player1, "Brainstone");
    }
}
