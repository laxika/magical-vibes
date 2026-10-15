package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChompingMastasaur.class, GrizzlyBears.class, HillGiant.class})
class ChompingMastasaurTest extends BaseCardTest {

    @Test
    @DisplayName("Entering still seeks when there is no card to discard")
    void enteringWithEmptyHandStillSeeks() {
        harness.setHand(player1, List.of(new ChompingMastasaur()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Attacking still seeks when there is no card to discard")
    void attackingWithEmptyHandStillSeeks() {
        addCreatureReady(player1, new ChompingMastasaur());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Seeking completes before choosing the reflexive damage trigger's target")
    void seeksBeforeChoosingDamageTarget() {
        harness.setHand(player1, List.of(new ChompingMastasaur(), new HillGiant()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Entering discards, seeks a nonland card, and deals damage equal to the discarded card's mana value")
    void enteringTriggersDiscardSeekAndDamage() {
        harness.setHand(player1, List.of(new ChompingMastasaur(), new HillGiant()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Attacking triggers the same discard, seek, and damage ability")
    void attackingTriggersDiscardSeekAndDamage() {
        addCreatureReady(player1, new ChompingMastasaur());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 10);
    }
}
