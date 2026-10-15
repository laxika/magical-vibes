package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheAnima;
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

@CardUsed({JundPanorama.class, Swamp.class, Mountain.class, Forest.class, Plains.class,
        Island.class, DruidOfTheAnima.class})
class JundPanoramaTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces one colorless mana")
    void tapAddsColorless() {
        harness.addToBattlefield(player1, new JundPanorama());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the search ability sacrifices Jund Panorama and presents only basic Swamp, Mountain, or Forest")
    void searchPresentsOnlySwampMountainForest() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jund Panorama");
        harness.assertInGraveyard(player1, "Jund Panorama");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Swamp")
                        || c.getName().equals("Mountain")
                        || c.getName().equals("Forest"))
                .anyMatch(c -> c.getName().equals("Swamp"))
                .anyMatch(c -> c.getName().equals("Mountain"))
                .anyMatch(c -> c.getName().equals("Forest"))
                .noneMatch(c -> c.getName().equals("Plains") || c.getName().equals("Island"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Chosen basic land enters the battlefield tapped")
    void chosenLandEntersTapped() {
        activateSearch();

        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Swamp") && p.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find")
    void canFailToFind() {
        activateSearch();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(com.github.laxika.magicalvibes.model.CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new JundPanorama());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Swamp(), new Mountain(), new Forest(),
                new Plains(), new Island(), new DruidOfTheAnima()));
    }

    @Test
    @DisplayName("Search costs are paid before the ability resolves")
    void searchCostsPaidImmediately() {
        activateSearch();

        harness.assertNotOnBattlefield(player1, "Jund Panorama");
        harness.assertInGraveyard(player1, "Jund Panorama");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
    }

    @Test
    @DisplayName("The search cannot use the same Panorama to pay its mana cost")
    void cannotSearchWithoutMana() {
        harness.addToBattlefield(player1, new JundPanorama());
        setupLibrary();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Jund Panorama");
        harness.assertNotInGraveyard(player1, "Jund Panorama");
        assertThat(findPermanent(player1, "Jund Panorama").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Panorama cannot activate the search ability")
    void cannotSearchWhileTapped() {
        harness.addToBattlefield(player1, new JundPanorama());
        harness.tapPermanent(player1, 0);
        setupLibrary();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Jund Panorama");
        harness.assertNotInGraveyard(player1, "Jund Panorama");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching an empty library still sacrifices Panorama and completes")
    void emptyLibrarySearchCompletes() {
        harness.addToBattlefield(player1, new JundPanorama());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jund Panorama");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
