package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DruidOfTheAnima;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NayaPanorama.class, Mountain.class, Forest.class, Plains.class, Swamp.class,
        Island.class, DruidOfTheAnima.class})
class NayaPanoramaTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces one colorless mana")
    void tapAddsColorless() {
        harness.addToBattlefield(player1, new NayaPanorama());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the search ability sacrifices Naya Panorama and presents only basic Mountain, Forest, or Plains")
    void searchPresentsOnlyMountainForestPlains() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Naya Panorama");
        harness.assertInGraveyard(player1, "Naya Panorama");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Mountain")
                        || c.getName().equals("Forest")
                        || c.getName().equals("Plains"))
                .anyMatch(c -> c.getName().equals("Mountain"))
                .anyMatch(c -> c.getName().equals("Forest"))
                .anyMatch(c -> c.getName().equals("Plains"))
                .noneMatch(c -> c.getName().equals("Swamp") || c.getName().equals("Island"));
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
                .anyMatch(p -> p.getCard().getName().equals("Mountain") && p.isTapped());
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

    private void activateSearch() {
        harness.addToBattlefield(player1, new NayaPanorama());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Mountain(), new Forest(), new Plains(),
                new Swamp(), new Island(), new DruidOfTheAnima()));
    }

    @Test
    @DisplayName("Search costs are paid before the ability resolves")
    void paysCostsBeforeResolution() {
        activateSearch();

        harness.assertNotOnBattlefield(player1, "Naya Panorama");
        harness.assertInGraveyard(player1, "Naya Panorama");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("A tapped Panorama cannot activate its search ability")
    void cannotSearchAfterTappingForMana() {
        harness.addToBattlefield(player1, new NayaPanorama());
        setupLibrary();
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Naya Panorama");
        harness.assertNotInGraveyard(player1, "Naya Panorama");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Search requires one mana from another source")
    void cannotSearchWithoutMana() {
        harness.addToBattlefield(player1, new NayaPanorama());
        setupLibrary();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "Naya Panorama").isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Naya Panorama");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Search completes when the library has no eligible cards")
    void noMatchingCards() {
        activateSearch();
        harness.setLibrary(player1, List.of(new Swamp(), new Island(), new NayaPanorama(),
                new DruidOfTheAnima()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Naya Panorama");
    }

    @Test
    @DisplayName("Search completes with an empty library")
    void emptyLibrary() {
        activateSearch();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Naya Panorama");
    }

    @Test
    @DisplayName("The ability searches its controller's library and puts the land under their control")
    void searchesControllersLibrary() {
        harness.addToBattlefield(player2, new NayaPanorama());
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(findPermanent(player2, "Forest").isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Naya Panorama");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Plains can be selected and enters tapped")
    void chosenPlainsEntersTapped() {
        activateSearch();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 2);

        assertThat(findPermanent(player1, "Plains").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Failing to find still shuffles the library and leaves its cards there")
    void failingToFindStillShuffles() {
        activateSearch();
        var libraryCards = List.copyOf(gd.playerDecks.get(player1.getId()));
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(libraryCards);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
