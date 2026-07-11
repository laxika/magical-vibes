package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NaturalOrderTest extends BaseCardTest {

    // ===== Casting (additional cost: sacrifice a green creature) =====

    @Test
    @DisplayName("Casting sacrifices the chosen green creature and puts the spell on the stack")
    void castingSacrificesGreenCreature() {
        Permanent greenCreature = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(greenCreature);

        harness.setHand(player1, List.of(new NaturalOrder()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorceryWithSacrifice(player1, 0, greenCreature.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutSacrifice() {
        harness.setHand(player1, List.of(new NaturalOrder()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice a non-green creature")
    void cannotSacrificeNonGreenCreature() {
        Permanent blueCreature = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player1.getId()).add(blueCreature);

        harness.setHand(player1, List.of(new NaturalOrder()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, blueCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green creature");
    }

    // ===== Resolving (search library for a green creature) =====

    @Test
    @DisplayName("Resolving presents only green creature cards from the library")
    void resolvingPresentsOnlyGreenCreatures() {
        castNaturalOrder();
        setupLibrary();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Llanowar Elves", "Grizzly Bears");
    }

    @Test
    @DisplayName("Search destination is the battlefield")
    void searchDestinationIsBattlefield() {
        castNaturalOrder();
        setupLibrary();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Choosing a creature puts it onto the battlefield, not into hand")
    void choosingPutsCreatureOntoBattlefield() {
        castNaturalOrder();
        setupLibrary();

        harness.passBothPriorities();

        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals(chosenName));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals(chosenName));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Helpers =====

    private void castNaturalOrder() {
        Permanent greenCreature = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(greenCreature);

        harness.setHand(player1, List.of(new NaturalOrder()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorceryWithSacrifice(player1, 0, greenCreature.getId());
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        // LlanowarElves + GrizzlyBears: green creatures; AirElemental: blue creature; Plains: basic land.
        deck.addAll(List.of(new LlanowarElves(), new GrizzlyBears(), new AirElemental(), new Plains()));
    }
}
