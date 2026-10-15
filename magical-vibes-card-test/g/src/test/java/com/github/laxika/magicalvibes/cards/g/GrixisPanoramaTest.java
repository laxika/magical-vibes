package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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

@CardUsed({GrixisPanorama.class, Island.class, Swamp.class, Mountain.class, Forest.class, Plains.class})
class GrixisPanoramaTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces one colorless mana")
    void tapAddsColorless() {
        harness.addToBattlefield(player1, new GrixisPanorama());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the search ability sacrifices Grixis Panorama and presents only basic Island, Swamp, or Mountain")
    void searchPresentsOnlyIslandSwampMountain() {
        activateSearch();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grixis Panorama");
        harness.assertInGraveyard(player1, "Grixis Panorama");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Island")
                        || c.getName().equals("Swamp")
                        || c.getName().equals("Mountain"))
                .anyMatch(c -> c.getName().equals("Island"))
                .anyMatch(c -> c.getName().equals("Swamp"))
                .anyMatch(c -> c.getName().equals("Mountain"))
                .noneMatch(c -> c.getName().equals("Forest") || c.getName().equals("Plains"));
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
                .anyMatch(p -> p.getCard().getName().equals("Island") && p.isTapped());
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

    @Test
    @DisplayName("Search costs mana and sacrifices Panorama before resolving")
    void searchPaysCostsImmediately() {
        activateSearch();

        harness.assertNotOnBattlefield(player1, "Grixis Panorama");
        harness.assertInGraveyard(player1, "Grixis Panorama");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Search cannot be activated without the generic mana payment")
    void searchRequiresMana() {
        harness.addToBattlefield(player1, new GrixisPanorama());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grixis Panorama");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Panorama cannot use its own mana ability and then pay the search tap cost")
    void tappedPanoramaCannotSearch() {
        harness.addToBattlefield(player1, new GrixisPanorama());
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grixis Panorama");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Search with only ineligible lands resolves without finding a card")
    void searchWithNoEligibleLand() {
        harness.addToBattlefield(player1, new GrixisPanorama());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new GrixisPanorama()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Grixis Panorama");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching an empty library finishes normally")
    void searchEmptyLibrary() {
        harness.addToBattlefield(player1, new GrixisPanorama());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grixis Panorama");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new GrixisPanorama());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Island(), new Swamp(), new Mountain(), new Forest(), new Plains()));
    }
}
