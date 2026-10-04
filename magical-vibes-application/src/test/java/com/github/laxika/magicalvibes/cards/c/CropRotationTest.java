package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CropRotation.class, TreetopVillage.class, YavimayaWurm.class})
class CropRotationTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a land as an additional cost")
    void sacrificesLandAsAdditionalCost() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());

        harness.setHand(player1, List.of(new CropRotation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithSacrifice(player1, 0, null, land.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Treetop Village");
        harness.assertInGraveyard(player1, "Treetop Village");
    }

    @Test
    @DisplayName("Cannot sacrifice a nonland permanent")
    void cannotSacrificeNonlandPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new YavimayaWurm());

        harness.setHand(player1, List.of(new CropRotation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");

        harness.assertOnBattlefield(player1, "Yavimaya Wurm");
    }

    @Test
    @DisplayName("Searches for a land and puts it onto the battlefield")
    void searchesForLandToBattlefield() {
        Permanent sacrificedLand = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.setLibrary(player1, List.of(new YavimayaWurm(), new TreetopVillage()));

        harness.setHand(player1, List.of(new CropRotation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstantWithSacrifice(player1, 0, null, sacrificedLand.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactly("Treetop Village");

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals(search.params().cards().getFirst().getName()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolves and shuffles when no land is found")
    void resolvesWhenNoLandIsFound() {
        Permanent sacrificedLand = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.setLibrary(player1, List.of(new YavimayaWurm()));

        harness.setHand(player1, List.of(new CropRotation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstantWithSacrifice(player1, 0, null, sacrificedLand.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        harness.assertNotOnBattlefield(player1, "Treetop Village");
        harness.assertInGraveyard(player1, "Treetop Village");
    }

    @Test
    @DisplayName("May fail to find even when a land is available")
    void mayFailToFindAvailableLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.setLibrary(player1, List.of(new TreetopVillage(), new YavimayaWurm()));
        harness.setHand(player1, List.of(new CropRotation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithSacrifice(player1, 0, null, land.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Treetop Village");
        harness.assertInGraveyard(player1, "Treetop Village");
        harness.assertInGraveyard(player1, "Crop Rotation");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Resolves with an empty library after paying the land cost")
    void resolvesWithEmptyLibrary() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CropRotation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithSacrifice(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Treetop Village");
        harness.assertInGraveyard(player1, "Crop Rotation");
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Can sacrifice a tapped land and fetched lands retain their enters-tapped ability")
    void sacrificesTappedLandAndHonorsEntryAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        land.tap();
        harness.setLibrary(player1, List.of(new TreetopVillage()));
        harness.setHand(player1, List.of(new CropRotation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithSacrifice(player1, 0, null, land.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(found -> {
                    assertThat(found.getId()).isNotEqualTo(land.getId());
                    assertThat(found.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Treetop Village");
        harness.assertInGraveyard(player1, "Crop Rotation");
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Cannot pay the additional cost with an opponent's land")
    void cannotSacrificeOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TreetopVillage());
        harness.setHand(player1, List.of(new CropRotation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");

        harness.assertOnBattlefield(player2, "Treetop Village");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast without sacrificing a land")
    void cannotCastWithoutLandSacrifice() {
        harness.setHand(player1, List.of(new CropRotation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
