package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.t.ThinkingCap;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgencyOutfitter.class, MagnifyingGlass.class, ThinkingCap.class})
class AgencyOutfitterTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may ability puts both named cards from the graveyard and hand onto the battlefield")
    void findsNamedCardsInGraveyardAndHand() {
        harness.setGraveyard(player1, List.of(new MagnifyingGlass()));
        harness.setHand(player1, List.of(new AgencyOutfitter(), new ThinkingCap()));
        castAgencyOutfitter();

        resolveEnterTriggerAndAcceptSearch();

        harness.assertOnBattlefield(player1, "Magnifying Glass");
        harness.assertOnBattlefield(player1, "Thinking Cap");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the may ability searches the library for either named card")
    void findsNamedCardsInLibrary() {
        harness.setLibrary(player1, List.of(new MagnifyingGlass(), new ThinkingCap()));
        harness.setHand(player1, List.of(new AgencyOutfitter()));
        castAgencyOutfitter();

        resolveEnterTriggerAndAcceptSearch();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Magnifying Glass")).hasSize(1);
        assertThat(findPermanents(player1, "Thinking Cap")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability does not search")
    void decliningSearchDoesNothing() {
        harness.setGraveyard(player1, List.of(new MagnifyingGlass()));
        harness.setHand(player1, List.of(new AgencyOutfitter(), new ThinkingCap()));
        castAgencyOutfitter();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Magnifying Glass")).isEmpty();
        assertThat(findPermanents(player1, "Thinking Cap")).isEmpty();
        harness.assertInGraveyard(player1, "Magnifying Glass");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Thinking Cap");
    }

    @Test
    @DisplayName("Both library cards are selected before either enters the battlefield")
    void doesNotPutFirstCardOntoBattlefieldWhileStillSearching() {
        harness.setLibrary(player1, List.of(new MagnifyingGlass(), new ThinkingCap()));
        harness.setHand(player1, List.of(new AgencyOutfitter()));
        castAgencyOutfitter();
        resolveEnterTriggerAndAcceptSearch();

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Magnifying Glass")).isEmpty();
        assertThat(findPermanents(player1, "Thinking Cap")).isEmpty();
    }

    @Test
    @DisplayName("The controller may find Thinking Cap without finding Magnifying Glass")
    void canFailToFindMagnifyingGlassAndFindThinkingCap() {
        harness.setLibrary(player1, List.of(new MagnifyingGlass(), new ThinkingCap()));
        harness.setHand(player1, List.of(new AgencyOutfitter()));
        castAgencyOutfitter();
        resolveEnterTriggerAndAcceptSearch();

        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Magnifying Glass")).isEmpty();
        harness.assertOnBattlefield(player1, "Thinking Cap");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Magnifying Glass");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching with neither named card available completes without moving other cards")
    void noNamedCardsAvailable() {
        harness.setLibrary(player1, List.of(new AgencyOutfitter()));
        harness.setGraveyard(player1, List.of(new AgencyOutfitter()));
        harness.setHand(player1, List.of(new AgencyOutfitter()));
        castAgencyOutfitter();
        resolveEnterTriggerAndAcceptSearch();

        assertThat(findPermanents(player1, "Agency Outfitter")).hasSize(1);
        harness.assertInGraveyard(player1, "Agency Outfitter");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAgencyOutfitter() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTriggerAndAcceptSearch() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}
