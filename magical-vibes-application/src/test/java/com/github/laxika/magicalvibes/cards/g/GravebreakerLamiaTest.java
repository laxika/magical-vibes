package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SqueeTheImmortal;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GravebreakerLamia.class, GrizzlyBears.class, Plains.class, Swamp.class, SqueeTheImmortal.class})
class GravebreakerLamiaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers any library card for the graveyard")
    void searchesAnyLibraryCardIntoGraveyard() {
        Card creature = new GrizzlyBears();
        Card plains = new Plains();
        Card swamp = new Swamp();
        harness.setLibrary(player1, List.of(creature, plains, swamp));
        castLamia();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(creature, plains, swamp);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.GRAVEYARD);
        assertThat(search.params().canFailToFind()).isFalse();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, swamp);
    }

    @Test
    @DisplayName("Reduces the generic cost of a spell cast from the controller's graveyard")
    void reducesGraveyardSpellCost() {
        harness.addToBattlefield(player1, new GravebreakerLamia());
        harness.setGraveyard(player1, List.of(new SqueeTheImmortal()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce the cost of a spell cast from hand")
    void doesNotReduceHandSpellCost() {
        harness.addToBattlefield(player1, new GravebreakerLamia());
        harness.setHand(player1, List.of(new SqueeTheImmortal()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castLamia() {
        harness.setHand(player1, List.of(new GravebreakerLamia()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
