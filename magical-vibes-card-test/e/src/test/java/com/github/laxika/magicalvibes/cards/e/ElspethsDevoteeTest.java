package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElspethsDevotee.class, ElspethUndauntedHero.class, GrizzlyBears.class})
class ElspethsDevoteeTest extends BaseCardTest {

    @Test
    @DisplayName("May find Elspeth, Undaunted Hero from the library")
    void mayFindElspethFromLibrary() {
        Card elspeth = new ElspethUndauntedHero();
        harness.setLibrary(player1, List.of(elspeth, new GrizzlyBears()));

        enterDevotee();

        chooseToSearch();
        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(elspeth.getId());
        harness.handleMultipleCardsChosen(player1, List.of(elspeth.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(elspeth);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(elspeth);
    }

    @Test
    @DisplayName("May find Elspeth, Undaunted Hero from the graveyard")
    void mayFindElspethFromGraveyard() {
        Card elspeth = new ElspethUndauntedHero();
        harness.setGraveyard(player1, List.of(elspeth));
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));

        enterDevotee();

        chooseToSearch();
        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(elspeth.getId());
        harness.handleMultipleCardsChosen(player1, List.of(elspeth.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(elspeth);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(elspeth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("May decline to search")
    void mayDeclineToSearch() {
        Card elspeth = new ElspethUndauntedHero();
        harness.setLibrary(player1, List.of(elspeth));

        enterDevotee();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elspeth);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(elspeth);
    }

    private void enterDevotee() {
        harness.setHand(player1, List.of(new ElspethsDevotee()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void chooseToSearch() {
        harness.handleMayAbilityChosen(player1, true);
    }
}
