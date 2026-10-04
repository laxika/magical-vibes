package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AkrasanSquire;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MistveilPlains;
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

@CardUsed({EsperPanorama.class, Forest.class, Plains.class, Island.class, Swamp.class, Mountain.class, AkrasanSquire.class, MistveilPlains.class})
class EsperPanoramaTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces one colorless mana")
    void tapAddsColorless() {
        harness.addToBattlefield(player1, new EsperPanorama());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the search ability sacrifices Esper Panorama and presents only basic Plains, Island, or Swamp")
    void searchPresentsOnlyPlainsIslandSwamp() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Esper Panorama");
        harness.assertInGraveyard(player1, "Esper Panorama");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Plains")
                        || c.getName().equals("Island")
                        || c.getName().equals("Swamp"))
                .anyMatch(c -> c.getName().equals("Plains"))
                .anyMatch(c -> c.getName().equals("Island"))
                .anyMatch(c -> c.getName().equals("Swamp"))
                .noneMatch(c -> c.getName().equals("Forest") || c.getName().equals("Mountain"));
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
                .anyMatch(p -> p.getCard().getName().equals("Plains") && p.isTapped());
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
    @DisplayName("Search costs are paid before resolution and the ability uses the stack")
    void paysCostsBeforeResolution() {
        activateSearch();

        harness.assertNotOnBattlefield(player1, "Esper Panorama");
        harness.assertInGraveyard(player1, "Esper Panorama");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A tapped Panorama cannot activate the search ability")
    void tappedPanoramaCannotSearch() {
        harness.addToBattlefield(player1, new EsperPanorama());
        harness.tapPermanent(player1, 0);
        setupLibrary();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Esper Panorama");
        harness.assertNotInGraveyard(player1, "Esper Panorama");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("An empty library does not prevent activation or resolution")
    void searchEmptyLibrary() {
        activateSearch();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Esper Panorama");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with no eligible land finishes the search without adding a permanent")
    void searchWithNoEligibleLand() {
        activateSearch();
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new AkrasanSquire(), new EsperPanorama()));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An Island can be selected and enters tapped under the searcher's control")
    void chosenIslandEntersTapped() {
        activateSearch();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(p -> {
                    assertThat(p.getCard().getName()).isEqualTo("Island");
                    assertThat(p.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5)
                .noneMatch(c -> c.getName().equals("Island"));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Swamp can be selected and enters tapped under the searcher's control")
    void chosenSwampEntersTapped() {
        activateSearch();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(p -> {
                    assertThat(p.getCard().getName()).isEqualTo("Swamp");
                    assertThat(p.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5)
                .noneMatch(c -> c.getName().equals("Swamp"));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A nonbasic Plains is excluded even though it has an eligible land type")
    void excludesNonbasicPlains() {
        activateSearch();
        Plains basicPlains = new Plains();
        harness.setLibrary(player1, List.of(new MistveilPlains(), basicPlains));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(basicPlains);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Mistveil Plains");
        assertThat(gd.playerDecks.get(player1.getId()))
                .singleElement().satisfies(c -> assertThat(c.getName()).isEqualTo("Mistveil Plains"));
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new EsperPanorama());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new Island(), new Swamp(), new Mountain(), new AkrasanSquire()));
    }
}
