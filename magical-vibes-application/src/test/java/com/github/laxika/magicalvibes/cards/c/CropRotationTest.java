package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CropRotationTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a land as an additional cost")
    void sacrificesLandAsAdditionalCost() {
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(land);

        harness.setHand(player1, List.of(new CropRotation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithSacrifice(player1, 0, null, land.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot sacrifice a nonland permanent")
    void cannotSacrificeNonlandPermanent() {
        Permanent creature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        harness.setHand(player1, List.of(new CropRotation()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Searches for a land and puts it onto the battlefield")
    void searchesForLandToBattlefield() {
        Permanent sacrificedLand = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(sacrificedLand);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Plains(), new Forest()));

        harness.setHand(player1, List.of(new CropRotation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstantWithSacrifice(player1, 0, null, sacrificedLand.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Plains", "Forest");

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals(search.params().cards().getFirst().getName()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
