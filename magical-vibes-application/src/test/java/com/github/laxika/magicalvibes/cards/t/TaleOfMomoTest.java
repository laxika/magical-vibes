package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({TaleOfMomo.class, ExpeditionEnvoy.class, Forest.class, GrizzlyBears.class})
class TaleOfMomoTest extends BaseCardTest {

    @Test
    @DisplayName("Searches the library and graveyard for an Ally creature")
    void searchesForAllyCreature() {
        Card libraryAlly = new ExpeditionEnvoy();
        Card graveyardAlly = new ExpeditionEnvoy();
        Card libraryNonAlly = new GrizzlyBears();
        Card graveyardNonAlly = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryAlly, libraryNonAlly));
        harness.setGraveyard(player1, List.of(graveyardAlly, graveyardNonAlly));
        castWithFullCost();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(libraryAlly.getId(), graveyardAlly.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardAlly.getId()));

        harness.assertInHand(player1, "Expedition Envoy");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardAlly);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(libraryAlly, libraryNonAlly);
    }

    @Test
    @DisplayName("Costs two less after a creature leaves under your control")
    void costsTwoLessAfterCreatureLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        harness.setLibrary(player1, List.of(new ExpeditionEnvoy()));
        harness.setHand(player1, List.of(new TaleOfMomo()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Does not reduce the cost after only a land leaves")
    void doesNotReduceCostAfterLandLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, land));
        harness.setHand(player1, List.of(new TaleOfMomo()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not reduce the cost after an opponent's creature leaves")
    void doesNotReduceCostAfterOpponentsCreatureLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        harness.setHand(player1, List.of(new TaleOfMomo()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWithFullCost() {
        harness.setHand(player1, List.of(new TaleOfMomo()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
