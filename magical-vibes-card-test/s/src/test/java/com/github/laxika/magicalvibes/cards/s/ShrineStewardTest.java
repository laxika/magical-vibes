package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TwistedEmbrace;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoShintaiOfSharedPurpose;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShrineSteward.class, TwistedEmbrace.class, Forest.class, GoShintaiOfSharedPurpose.class})
class ShrineStewardTest extends BaseCardTest {

    @Test
    @DisplayName("Offers Aura and Shrine cards from the library")
    void offersAuraAndShrineCards() {
        TwistedEmbrace aura = new TwistedEmbrace();
        GoShintaiOfSharedPurpose shrine = new GoShintaiOfSharedPurpose();
        Forest land = new Forest();
        castSteward(List.of(land, aura, shrine));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(aura, shrine);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(aura);
        assertThat(gd.playerDecks.get(player1.getId())).contains(shrine, land);
    }

    @Test
    @DisplayName("Declining the search leaves the library unchanged")
    void decliningSearchLeavesLibraryUnchanged() {
        TwistedEmbrace aura = new TwistedEmbrace();
        GoShintaiOfSharedPurpose shrine = new GoShintaiOfSharedPurpose();
        List<Card> library = List.of(aura, shrine);
        castSteward(library);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura, shrine);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(aura, shrine);
    }

    @Test
    @DisplayName("Does not offer cards that are neither Auras nor Shrines")
    void filtersNonAuraAndNonShrineCards() {
        Forest land = new Forest();
        castSteward(List.of(land));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Can put an enchantment creature Shrine into hand without casting it")
    void findsCreatureShrine() {
        GoShintaiOfSharedPurpose shrine = new GoShintaiOfSharedPurpose();
        TwistedEmbrace aura = new TwistedEmbrace();
        castSteward(List.of(aura, shrine));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shrine);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("May fail to find even when an eligible card is present")
    void mayFailToFind() {
        TwistedEmbrace aura = new TwistedEmbrace();
        GoShintaiOfSharedPurpose shrine = new GoShintaiOfSharedPurpose();
        castSteward(List.of(aura, shrine));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(aura, shrine);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    @Test
    @DisplayName("Searching an empty library completes without a card choice")
    void searchesEmptyLibrary() {
        castSteward(List.of());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }

    private void castSteward(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ShrineSteward()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }
}
