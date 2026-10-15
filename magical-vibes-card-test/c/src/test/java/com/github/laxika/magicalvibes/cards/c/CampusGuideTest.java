package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CampusGuide.class, Plains.class, EvolvingWilds.class})
class CampusGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability offers basic lands and puts the choice on top")
    void acceptsBasicLandSearch() {
        Card basicLand = new Plains();
        Card nonland = new CampusGuide();
        setup(List.of(basicLand, nonland));

        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(basicLand);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(basicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB ability does not search")
    void declinesBasicLandSearch() {
        Card basicLand = new Plains();
        Card nonland = new CampusGuide();
        setup(List.of(basicLand, nonland));

        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basicLand, nonland);
    }

    @Test
    @DisplayName("A basic land search may fail to find even when a basic land is present")
    void mayFailToFindBasicLand() {
        Card basicLand = new Plains();
        setup(List.of(basicLand));
        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basicLand);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Searching a library containing only a nonbasic land finds nothing")
    void nonbasicLandIsNotEligible() {
        Card nonbasicLand = new EvolvingWilds();
        setup(List.of(nonbasicLand));
        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonbasicLand);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting a search with an empty library finishes normally")
    void searchesEmptyLibrary() {
        setup(List.of());
        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void setup(List<Card> library) {
        harness.setHand(player1, List.of(new CampusGuide()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, library);
        harness.castCreature(player1, 0);
    }

    private void resolveEtbMayPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
