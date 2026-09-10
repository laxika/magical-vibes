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
        Permanent land = new Permanent(new TreetopVillage());
        gd.playerBattlefields.get(player1.getId()).add(land);

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
        Permanent creature = new Permanent(new YavimayaWurm());
        gd.playerBattlefields.get(player1.getId()).add(creature);

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
        Permanent sacrificedLand = new Permanent(new TreetopVillage());
        gd.playerBattlefields.get(player1.getId()).add(sacrificedLand);
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
        Permanent sacrificedLand = new Permanent(new TreetopVillage());
        gd.playerBattlefields.get(player1.getId()).add(sacrificedLand);
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
}
