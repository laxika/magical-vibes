package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CelestialRegulator;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RaffinesTower;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObscuraStorefront.class, CelestialRegulator.class, Island.class, Plains.class, Swamp.class,
        Forest.class, Mountain.class, RaffinesTower.class})
class ObscuraStorefrontTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices Obscura Storefront before the search trigger resolves")
    void enteringSacrificesIt() {
        ObscuraStorefront storefront = new ObscuraStorefront();
        harness.setHand(player1, List.of(storefront));

        harness.playLand(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(storefront.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(storefront.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(storefront.getId()));
    }

    @Test
    @DisplayName("Searches for a basic Plains, Island, or Swamp and puts it onto the battlefield tapped")
    void searchesAllowedBasicLand() {
        playStorefront();
        Card plains = new Plains();
        Card island = new Island();
        Card swamp = new Swamp();
        setLibrary(plains, island, swamp, new CelestialRegulator());

        resolveToSearchPrompt();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(plains, island, swamp);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters tapped and controller gains 1 life")
    void chosenLandEntersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        playStorefront();
        Card island = new Island();
        setLibrary(island, new Plains(), new Swamp());

        resolveToSearchPrompt();
        harness.handleCardChosen(player1, 0);

        Permanent chosenLand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(island.getId()))
                .findFirst().orElseThrow();
        assertThat(chosenLand.isTapped()).isTrue();
        harness.assertLife(player1, 21);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No matching land leaves the search resolved and still gains 1 life")
    void noMatchingLandDoesNotPrompt() {
        harness.setLife(player1, 20);
        playStorefront();
        setLibrary(new CelestialRegulator());

        resolveToSearchPrompt();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Sacrifice and search resolve as separate triggers with a priority window")
    void searchWaitsForReflexiveTrigger() {
        harness.setLife(player1, 20);
        playStorefront();
        setLibrary(new Island());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Obscura Storefront");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.assertLife(player1, 20);
        harness.handleCardChosen(player1, 0);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Can fail to find an eligible basic land and still gain life")
    void decliningSearchStillGainsLife() {
        harness.setLife(player1, 20);
        playStorefront();
        Card island = new Island();
        setLibrary(island);

        resolveToSearchPrompt();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Excludes basic lands of other types and nonbasic lands with allowed types")
    void excludesDisallowedLands() {
        playStorefront();
        Card island = new Island();
        setLibrary(new Forest(), new Mountain(), new RaffinesTower(), island);

        resolveToSearchPrompt();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(island);
    }

    @Test
    @DisplayName("No search or life gain if Storefront leaves before the sacrifice trigger resolves")
    void cannotSacrificeDepartedSource() {
        harness.setLife(player1, 20);
        playStorefront();
        Card island = new Island();
        setLibrary(island);
        Permanent storefront = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.getPermanentRemovalService().removePermanentToHand(gd, storefront);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        harness.assertInHand(player1, "Obscura Storefront");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Empty library still gains life after sacrificing Storefront")
    void emptyLibraryStillGainsLife() {
        harness.setLife(player1, 20);
        playStorefront();
        setLibrary();

        resolveToSearchPrompt();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player1, 21);
    }

    private void playStorefront() {
        harness.setHand(player1, List.of(new ObscuraStorefront()));
        harness.playLand(player1, 0);
    }

    private void resolveToSearchPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
