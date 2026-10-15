package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EscapeTunnel;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TopiaryPanther.class, Plains.class, GrizzlyBears.class, EscapeTunnel.class})
class TopiaryPantherTest extends BaseCardTest {

    @Test
    @DisplayName("Basic landcycling discards the card and searches for a basic land")
    void basicLandcyclingSearchesForBasicLand() {
        harness.setHand(player1, List.of(new TopiaryPanther()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Topiary Panther");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement()
                .satisfies(card -> assertThat(card.hasType(CardType.LAND)).isTrue())
                .satisfies(card -> assertThat(card.getSupertypes()).contains(CardSupertype.BASIC));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof Plains);
    }

    @Test
    void discardsAsAnActivationCostBeforeSearching() {
        harness.setHand(player1, List.of(new TopiaryPanther()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Topiary Panther");
        harness.assertInGraveyard(player1, "Topiary Panther");
        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Plains");
    }

    @Test
    void cannotCycleWithoutGreenMana() {
        harness.setHand(player1, List.of(new TopiaryPanther()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Topiary Panther");
        harness.assertNotInGraveyard(player1, "Topiary Panther");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCycleWithOnlyOneMana() {
        harness.setHand(player1, List.of(new TopiaryPanther()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Topiary Panther");
        harness.assertNotInGraveyard(player1, "Topiary Panther");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canFailToFindEvenWhenABasicLandIsAvailable() {
        harness.setHand(player1, List.of(new TopiaryPanther()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Topiary Panther");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void excludesNonbasicLandsAndNonlandCards() {
        harness.setHand(player1, List.of(new TopiaryPanther()));
        harness.setLibrary(player1, List.of(new EscapeTunnel(), new TopiaryPanther()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Topiary Panther");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cyclesWithAnEmptyLibrary() {
        harness.setHand(player1, List.of(new TopiaryPanther()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Topiary Panther");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tramplesOverABlocker() {
        addCreatureReady(player1, new TopiaryPanther());
        var blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 4));

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Topiary Panther");
    }
}
