package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TropicalIsland;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BantPanorama.class, Forest.class, Plains.class, Island.class, Swamp.class,
        Mountain.class, GrizzlyBears.class, TropicalIsland.class})
class BantPanoramaTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces one colorless mana")
    void tapAddsColorless() {
        harness.addToBattlefield(player1, new BantPanorama());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the search ability sacrifices Bant Panorama and presents only basic Forest, Plains, or Island")
    void searchPresentsOnlyForestPlainsIsland() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bant Panorama");
        harness.assertInGraveyard(player1, "Bant Panorama");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Forest")
                        || c.getName().equals("Plains")
                        || c.getName().equals("Island"))
                .anyMatch(c -> c.getName().equals("Forest"))
                .anyMatch(c -> c.getName().equals("Plains"))
                .anyMatch(c -> c.getName().equals("Island"))
                .noneMatch(c -> c.getName().equals("Swamp") || c.getName().equals("Mountain"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters the battlefield tapped")
    void chosenLandEntersTapped() {
        activateSearch();

        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest") && p.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        activateSearch();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(com.github.laxika.magicalvibes.model.CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Search costs are paid before the ability resolves")
    void costsPaidBeforeResolution() {
        activateSearch();

        harness.assertNotOnBattlefield(player1, "Bant Panorama");
        harness.assertInGraveyard(player1, "Bant Panorama");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("A tapped Panorama cannot activate its search ability")
    void tappedPanoramaCannotSearch() {
        harness.addToBattlefield(player1, new BantPanorama());
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Bant Panorama");
        harness.assertNotInGraveyard(player1, "Bant Panorama");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Plains and Island can also be found and enter tapped")
    void otherAllowedLandsEnterTapped(int choice) {
        activateSearch();
        String landName = choice == 1 ? "Plains" : "Island";

        harness.passBothPriorities();
        harness.handleCardChosen(player1, choice);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(p -> {
                    assertThat(p.getCard().getName()).isEqualTo(landName);
                    assertThat(p.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5)
                .noneMatch(c -> c.getName().equals(landName));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A nonbasic Forest Island is excluded from the search")
    void excludesNonbasicLandWithAllowedSubtypes() {
        activateSearch();
        harness.setLibrary(player1, List.of(new TropicalIsland(), new Forest()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement().satisfies(c -> assertThat(c.getName()).isEqualTo("Forest"));
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerDecks.get(player1.getId())).singleElement()
                .satisfies(c -> assertThat(c.getName()).isEqualTo("Tropical Island"));
    }

    @Test
    @DisplayName("An empty library finishes the search without requesting a choice")
    void emptyLibraryFinishesSearch() {
        activateSearch();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Bant Panorama");
    }

    @Test
    @DisplayName("A library without eligible lands finishes without putting anything onto the battlefield")
    void noEligibleLandsFinishesSearch() {
        activateSearch();
        harness.setLibrary(player1, List.of(new Swamp(), new Mountain(), new GrizzlyBears()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Bant Panorama");
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new BantPanorama());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1,
                List.of(new Forest(), new Plains(), new Island(), new Swamp(), new Mountain(), new GrizzlyBears()));
    }
}
