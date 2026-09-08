package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CompassGnome.class, CavernousMaw.class, Forest.class, GrizzlyBears.class})
class CompassGnomeTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability offers basic lands and Caves and puts the choice on top")
    void acceptsBasicLandOrCaveSearch() {
        Card basicLand = new Forest();
        Card cave = new CavernousMaw();
        Card nonmatching = new GrizzlyBears();
        castGnome(List.of(basicLand, cave, nonmatching));

        acceptEtbSearch();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.TOP_OF_LIBRARY);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(basicLand, cave);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isIn(basicLand, cave);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB ability skips the search")
    void declinesSearch() {
        Card basicLand = new Forest();
        castGnome(List.of(basicLand));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castGnome(List<Card> library) {
        harness.setHand(player1, List.of(new CompassGnome()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, library);
        harness.castCreature(player1, 0);
    }

    private void acceptEtbSearch() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}
