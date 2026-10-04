package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SqueeTheImmortal;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.UnderworldCharger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({GravebreakerLamia.class, GrizzlyBears.class, Plains.class, Swamp.class,
        SqueeTheImmortal.class, UnderworldCharger.class})
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

        harness.handleCardChosen(player1, 0);

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

    @Test
    void doesNotReduceOpponentsGraveyardSpellCost() {
        harness.addToBattlefield(player2, new GravebreakerLamia());
        harness.setGraveyard(player1, List.of(new SqueeTheImmortal()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReduceSpellsCastFromExile() {
        harness.addToBattlefield(player1, new GravebreakerLamia());
        Card squee = new SqueeTheImmortal();
        harness.setExile(player1, List.of(squee));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, squee.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleLamiasDoNotReduceColoredMana() {
        harness.addToBattlefield(player1, new GravebreakerLamia());
        harness.addToBattlefield(player1, new GravebreakerLamia());
        harness.setGraveyard(player1, List.of(new SqueeTheImmortal()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesEscapeCostAndPreservesAdditionalExileCost() {
        harness.addToBattlefield(player1, new GravebreakerLamia());
        Card charger = new UnderworldCharger();
        Card plains = new Plains();
        Card swamp = new Swamp();
        Card lamia = new GravebreakerLamia();
        harness.setGraveyard(player1, List.of(charger, plains, swamp, lamia));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(plains, swamp, lamia);
    }

    @Test
    void multipleLamiasStackTheirReductions() {
        harness.addToBattlefield(player1, new GravebreakerLamia());
        harness.addToBattlefield(player1, new GravebreakerLamia());
        harness.setGraveyard(player1, List.of(new UnderworldCharger(),
                new Plains(), new Swamp(), new GravebreakerLamia()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void emptyLibraryCompletesWithoutAChoice() {
        harness.setLibrary(player1, List.of());

        castLamia();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Gravebreaker Lamia");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void unrestrictedSearchCannotFailToFind() {
        Card swamp = new Swamp();
        harness.setLibrary(player1, List.of(swamp));
        castLamia();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(swamp);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void combatDamageGainsLifeForItsController() {
        addCreatureReady(player1, new GravebreakerLamia());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 16);
    }

    private void castLamia() {
        harness.setHand(player1, List.of(new GravebreakerLamia()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
