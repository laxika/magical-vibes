package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SibsigAppraiser.class})
class SibsigAppraiserTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts one of the top two cards into hand and the other into the graveyard")
    void choosesOneCardForHandAndPutsTheOtherInGraveyard() {
        Card chosen = new SibsigAppraiser();
        Card other = new SibsigAppraiser();
        harness.setLibrary(player1, List.of(chosen, other));
        castSibsigAppraiser();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gameData.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(other);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With one card in the library, the card goes into hand")
    void oneCardInLibrary() {
        Card onlyCard = new SibsigAppraiser();
        harness.setLibrary(player1, List.of(onlyCard));
        castSibsigAppraiser();

        assertThat(gd.playerHands.get(player1.getId())).contains(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With an empty library, the ETB does not move any card")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        castSibsigAppraiser();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing no card is illegal when two cards are available")
    void mustPutOneCardIntoHand() {
        Card first = new SibsigAppraiser();
        Card second = new SibsigAppraiser();
        harness.setLibrary(player1, List.of(first, second));
        castSibsigAppraiser();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("Choosing the second card leaves cards below the top two untouched")
    void onlyLooksAtTopTwoCards() {
        Card first = new SibsigAppraiser();
        Card second = new SibsigAppraiser();
        Card third = new SibsigAppraiser();
        harness.setLibrary(player1, List.of(first, second, third));
        castSibsigAppraiser();

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castSibsigAppraiser() {
        harness.setHand(player1, List.of(new SibsigAppraiser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
