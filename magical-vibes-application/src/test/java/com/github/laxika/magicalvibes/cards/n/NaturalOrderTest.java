package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AngelOfJubilation;
import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({NaturalOrder.class, GrizzlyBears.class, ElvishRanger.class, HornedTurtle.class, Plains.class, NaturesLore.class})
class NaturalOrderTest extends BaseCardTest {

    // ===== Casting (additional cost: sacrifice a green creature) =====

    @Test
    @DisplayName("Casting sacrifices the chosen green creature and puts the spell on the stack")
    void castingSacrificesGreenCreature() {
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new NaturalOrder()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorceryWithSacrifice(player1, 0, greenCreature.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
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
        Permanent blueCreature = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());

        harness.setHand(player1, List.of(new NaturalOrder()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, blueCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("green creature");
    }

    @Test
    @CardUsed(AngelOfJubilation.class)
    @DisplayName("Cannot cast when sacrificing creatures as costs is prohibited")
    void cannotCastWhenCreatureSacrificesAreProhibited() {
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AngelOfJubilation());

        harness.setHand(player1, List.of(new NaturalOrder()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, greenCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Natural Order");
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
                .containsExactlyInAnyOrder("Elvish Ranger", "Grizzly Bears");
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
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, chosenName);
        harness.assertNotInHand(player1, chosenName);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolving sends Natural Order to its owner's graveyard")
    void resolvingSendsSpellToGraveyard() {
        castNaturalOrder();
        setupLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Natural Order");
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Natural Order"));
    }

    @Test
    @DisplayName("May fail to find even when a green creature is available")
    void mayFailToFind() {
        castNaturalOrder();
        setupLibrary();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Elvish Ranger");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Natural Order");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Elvish Ranger", "Grizzly Bears", "Horned Turtle", "Nature's Lore", "Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No green creature in the library ends the search without prompting")
    void noGreenCreatureInLibrary() {
        castNaturalOrder();
        harness.setLibrary(player1, List.of(new NaturesLore(), new HornedTurtle(), new Plains()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Nature's Lore");
        harness.assertInGraveyard(player1, "Natural Order");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Nature's Lore", "Horned Turtle", "Plains");
    }

    // ===== Helpers =====

    private void castNaturalOrder() {
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new NaturalOrder()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorceryWithSacrifice(player1, 0, greenCreature.getId());
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(
                new ElvishRanger(), new GrizzlyBears(), new HornedTurtle(), new NaturesLore(), new Plains()));
    }
}
