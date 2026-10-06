package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResentfulRevelation.class})
class ResentfulRevelationTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one of the top three cards into hand and the rest into the graveyard")
    void choosesOneCardAndPutsRestInGraveyard() {
        Card chosen = new ResentfulRevelation();
        Card restOne = new ResentfulRevelation();
        Card restTwo = new ResentfulRevelation();
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo));

        castResentfulRevelation();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(restOne, restTwo);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Flashback resolves the spell and exiles it")
    void flashbackResolvesAndExiles() {
        Card flashbackCard = new ResentfulRevelation();
        Card chosen = new ResentfulRevelation();
        Card restOne = new ResentfulRevelation();
        Card restTwo = new ResentfulRevelation();
        harness.setGraveyard(player1, List.of(flashbackCard));
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveFlashback(player1, 0, null);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(restOne, restTwo);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(flashbackCard);
    }

    @Test
    void mustChooseOneCardWhenCardsAreAvailable() {
        Card first = new ResentfulRevelation();
        Card second = new ResentfulRevelation();
        Card third = new ResentfulRevelation();
        harness.setLibrary(player1, List.of(first, second, third));

        castResentfulRevelation();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, third);
    }

    @Test
    void leavesCardsBelowTheTopThreeInLibrary() {
        Card first = new ResentfulRevelation();
        Card second = new ResentfulRevelation();
        Card third = new ResentfulRevelation();
        Card fourth = new ResentfulRevelation();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        castResentfulRevelation();
        harness.handleMultipleCardsChosen(player1, List.of(third.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second).doesNotContain(fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
    }

    @Test
    void looksAtBothCardsWhenLibraryHasOnlyTwo() {
        Card chosen = new ResentfulRevelation();
        Card rest = new ResentfulRevelation();
        harness.setLibrary(player1, List.of(rest, chosen));

        castResentfulRevelation();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void putsTheOnlyLibraryCardIntoHandWithoutAChoice() {
        Card onlyCard = new ResentfulRevelation();
        harness.setLibrary(player1, List.of(onlyCard));

        castResentfulRevelation();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithAnEmptyLibraryWithoutAChoice() {
        harness.setLibrary(player1, List.of());

        castResentfulRevelation();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castResentfulRevelation() {
        harness.setHand(player1, List.of(new ResentfulRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
