package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CinderGlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BountifulLandscape.class, Forest.class, Island.class, Mountain.class, Plains.class, GrizzlyBears.class, CinderGlade.class})
class BountifulLandscapeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana produces one colorless")
    void tappingProducesColorlessMana() {
        harness.addToBattlefield(player1, new BountifulLandscape());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Search ability sacrifices the land and presents only basic Forests, Islands, and Mountains")
    void searchPresentsMatchingBasicLands() {
        harness.addToBattlefield(player1, new BountifulLandscape());
        setupLibrary();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bountiful Landscape");
        harness.assertInGraveyard(player1, "Bountiful Landscape");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getName().equals("Forest")
                        || card.getName().equals("Island")
                        || card.getName().equals("Mountain"))
                .noneMatch(card -> card.getName().equals("Plains") || card.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Cycling pays green, blue, and red, discards the card, and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new BountifulLandscape()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Bountiful Landscape");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void eachAllowedBasicLandEntersTapped(int selection) {
        harness.addToBattlefield(player1, new BountifulLandscape());
        setupLibrary();

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Bountiful Landscape");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, selection);

        String chosenName = List.of("Forest", "Island", "Mountain").get(selection);
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard().getName()).isEqualTo(chosenName);
            assertThat(permanent.isTapped()).isTrue();
        });
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4)
                .noneMatch(card -> card.getName().equals(chosenName));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonbasicLandWithMatchingTypesIsExcluded() {
        harness.addToBattlefield(player1, new BountifulLandscape());
        harness.setLibrary(player1, List.of(new CinderGlade(), new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getName()).containsExactly("Forest");
    }

    @Test
    void canFailToFindEvenWithMatchingLands() {
        harness.addToBattlefield(player1, new BountifulLandscape());
        setupLibrary();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Bountiful Landscape");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noMatchingLandsFinishesWithoutAChoice() {
        harness.addToBattlefield(player1, new BountifulLandscape());
        harness.setLibrary(player1, List.of(new Plains(), new CinderGlade(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryFinishesWithoutAChoice() {
        harness.addToBattlefield(player1, new BountifulLandscape());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bountiful Landscape");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedLandCannotBeSacrificedForSearch() {
        harness.addToBattlefield(player1, new BountifulLandscape());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Bountiful Landscape");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cyclingPaysCostsBeforeDrawResolves() {
        harness.setHand(player1, List.of(new BountifulLandscape()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Bountiful Landscape");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        for (ManaColor color : List.of(ManaColor.GREEN, ManaColor.BLUE, ManaColor.RED)) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"GREEN", "BLUE", "RED"})
    void cyclingRequiresEachColoredMana(ManaColor missingColor) {
        harness.setHand(player1, List.of(new BountifulLandscape()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        for (ManaColor color : List.of(ManaColor.GREEN, ManaColor.BLUE, ManaColor.RED)) {
            harness.addMana(player1, color == missingColor ? ManaColor.COLORLESS : color, 1);
        }

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Bountiful Landscape");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Mountain(), new Plains(), new GrizzlyBears()));
    }
}
