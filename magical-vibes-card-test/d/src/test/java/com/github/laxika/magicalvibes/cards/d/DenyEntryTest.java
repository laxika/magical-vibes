package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GaeasHerald;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DenyEntry.class, GrizzlyBears.class, Island.class, Millstone.class, GaeasHerald.class})
class DenyEntryTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and draws then discards a card")
    void countersCreatureAndLoots() {
        GrizzlyBears bears = new GrizzlyBears();
        Island remainingCard = new Island();
        Island drawnCard = new Island();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        DenyEntry denyEntry = new DenyEntry();
        harness.setHand(player2, List.of(denyEntry, remainingCard));
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard, drawnCard);

        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Deny Entry");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Cannot target a noncreature spell")
    void cannotTargetNoncreatureSpell() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new DenyEntry()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, millstone.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can discard a card that was in hand before drawing")
    void canDiscardExistingCard() {
        GrizzlyBears bears = new GrizzlyBears();
        Island existingCard = new Island();
        Island drawnCard = new Island();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new DenyEntry(), existingCard));
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(existingCard);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Does not draw or discard when the creature spell has already been countered")
    void doesNotLootWithMissingTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        DenyEntry firstCounter = new DenyEntry();
        DenyEntry secondCounter = new DenyEntry();
        Island existingCard = new Island();
        Island drawnCard = new Island();
        Island undrawnCard = new Island();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(firstCounter, secondCounter, existingCard));
        harness.setLibrary(player2, List.of(drawnCard, undrawnCard));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(undrawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(firstCounter, secondCounter, existingCard);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Still draws and discards when the targeted creature spell cannot be countered")
    void lootsWhenCreatureCannotBeCountered() {
        harness.addToBattlefield(player1, new GaeasHerald());
        GrizzlyBears bears = new GrizzlyBears();
        Island existingCard = new Island();
        Island drawnCard = new Island();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new DenyEntry(), existingCard));
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(existingCard, drawnCard);
        assertThat(gd.stack).anySatisfy(entry -> assertThat(entry.getCard()).isSameAs(bears));
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(existingCard);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }
}
