package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeceptiveLandscape.class, Plains.class, Swamp.class, Forest.class, GrizzlyBears.class, Island.class})
class DeceptiveLandscapeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana produces one colorless")
    void tappingProducesColorlessMana() {
        harness.addToBattlefield(player1, new DeceptiveLandscape());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Search ability sacrifices the land and presents only basic Plains, Swamps, and Forests")
    void searchPresentsMatchingBasicLands() {
        harness.addToBattlefield(player1, new DeceptiveLandscape());
        setupLibrary();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deceptive Landscape");
        harness.assertInGraveyard(player1, "Deceptive Landscape");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getName().equals("Plains")
                        || card.getName().equals("Swamp")
                        || card.getName().equals("Forest"))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Cycling pays white, black, and green, discards the card, and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new DeceptiveLandscape()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Deceptive Landscape");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Swamp(), new Forest(), new GrizzlyBears()));
    }

    @ParameterizedTest
    @CsvSource({"0, Plains", "1, Swamp", "2, Forest"})
    void searchedLandEntersTapped(int libraryIndex, String landName) {
        harness.addToBattlefield(player1, new DeceptiveLandscape());
        setupLibrary();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Deceptive Landscape");
        harness.assertNotOnBattlefield(player1, "Deceptive Landscape");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, libraryIndex);

        assertThat(findPermanent(player1, landName).isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3)
                .noneMatch(card -> card.getName().equals(landName));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayFailToFindEvenWithMatchingLands() {
        harness.addToBattlefield(player1, new DeceptiveLandscape());
        setupLibrary();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Deceptive Landscape");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cyclingCannotUseColorlessInsteadOfGreen() {
        harness.setHand(player1, List.of(new DeceptiveLandscape()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Deceptive Landscape");
        harness.assertNotInGraveyard(player1, "Deceptive Landscape");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void searchWithOnlyIneligibleCardsFinishesWithoutPuttingALandOntoBattlefield() {
        harness.addToBattlefield(player1, new DeceptiveLandscape());
        harness.setLibrary(player1, List.of(new Island(), new DeceptiveLandscape(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Deceptive Landscape");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
