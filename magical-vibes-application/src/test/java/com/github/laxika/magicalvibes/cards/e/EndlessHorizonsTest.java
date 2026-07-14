package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EndlessHorizonsTest extends BaseCardTest {

    // ===== ETB — search library for any number of Plains, exile them tracked with the source =====

    private void castAndResolveEtb(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new EndlessHorizons()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment → ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger → library search
    }

    @Test
    @DisplayName("ETB search offers only Plains cards from the library")
    void etbSearchOffersOnlyPlains() {
        castAndResolveEtb(List.of(new Plains(), new Forest(), new Plains(), new Forest()));

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("ETB exiles the chosen Plains tracked with the enchantment; non-Plains stay in the library")
    void etbExilesChosenPlainsTrackedWithSource() {
        castAndResolveEtb(List.of(new Plains(), new Forest(), new Plains(), new Forest()));

        UUID permId = harness.getPermanentId(player1, "Endless Horizons");

        gs.handleLibraryCardChosen(gd, player1, 0); // exile first Plains, re-prompt
        gs.handleLibraryCardChosen(gd, player1, 0); // exile second Plains, no matches remain → done

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(2);
        assertThat(gd.getCardsExiledByPermanent(permId)).allMatch(c -> c.getName().equals("Plains"));
        // The two Forests remain in the library.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).allMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Controller may stop the any-number search early")
    void etbCanStopEarly() {
        castAndResolveEtb(List.of(new Plains(), new Plains(), new Forest()));

        UUID permId = harness.getPermanentId(player1, "Endless Horizons");

        gs.handleLibraryCardChosen(gd, player1, 0);  // exile one Plains
        gs.handleLibraryCardChosen(gd, player1, -1); // decline the rest

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(1);
        // The unexiled Plains is still in the library.
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("With no Plains in the library, no search prompt is created")
    void etbNoPlainsNoSearch() {
        castAndResolveEtb(List.of(new Forest(), new Forest()));

        UUID permId = harness.getPermanentId(player1, "Endless Horizons");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
    }

    // ===== Upkeep — you may put an exiled card into your hand =====

    private UUID setupWithExiledPlains(int plainsCount) {
        harness.addToBattlefield(player1, new EndlessHorizons());
        UUID permId = harness.getPermanentId(player1, "Endless Horizons");
        for (int i = 0; i < plainsCount; i++) {
            gd.addToExile(player1.getId(), new Plains(), permId);
        }
        return permId;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, fires upkeep triggers
    }

    @Test
    @DisplayName("Accepting the upkeep trigger returns a single exiled Plains to hand")
    void upkeepReturnsSingleExiledPlainsToHand() {
        UUID permId = setupWithExiledPlains(1);
        UUID exiledId = gd.getCardsExiledByPermanent(permId).getFirst().getId();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(exiledId));
        assertThat(gd.getCardsExiledByPermanent(permId)).isEmpty();
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves the card exiled")
    void upkeepDeclineLeavesCardExiled() {
        UUID permId = setupWithExiledPlains(1);

        UUID exiledId = gd.getCardsExiledByPermanent(permId).getFirst().getId();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger → may prompt
        harness.handleMayAbilityChosen(player1, false);

        // The card is not returned: it stays exiled with the enchantment.
        assertThat(gd.getCardsExiledByPermanent(permId))
                .hasSize(1)
                .anyMatch(c -> c.getId().equals(exiledId));
    }

    @Test
    @DisplayName("With several exiled cards, the controller chooses one to return")
    void upkeepChoosesOneOfSeveral() {
        UUID permId = setupWithExiledPlains(3);
        UUID chosen = gd.getCardsExiledByPermanent(permId).getFirst().getId();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(chosen));
        assertThat(gd.getCardsExiledByPermanent(permId)).hasSize(2);
        assertThat(gd.getCardsExiledByPermanent(permId)).noneMatch(c -> c.getId().equals(chosen));
    }
}
