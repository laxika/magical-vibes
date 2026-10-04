package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FailedInspection.class, GrizzlyBears.class, Forest.class})
class FailedInspectionTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell, then draws and discards a card")
    void countersDrawsAndDiscards() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new FailedInspection(), new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Failed Inspection");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        harness.setHand(player2, List.of(new FailedInspection()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The newly drawn card can be chosen for discard")
    void canDiscardNewlyDrawnCard() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest retained = new Forest();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new FailedInspection(), retained));
        harness.setLibrary(player2, List.of(drawn));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained, drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(drawn);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Failed Inspection");
    }

    @Test
    @DisplayName("Does not draw or discard when its only target has left the stack")
    void noLootWhenTargetLeavesStack() {
        GrizzlyBears bears = new GrizzlyBears();
        Forest drawnByPlayer1 = new Forest();
        Forest player2HandCard = new Forest();
        Forest player2LibraryCard = new Forest();
        harness.setHand(player1, List.of(bears, new FailedInspection(), new Forest()));
        harness.setLibrary(player1, List.of(drawnByPlayer1));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setHand(player2, List.of(new FailedInspection(), player2HandCard));
        harness.setLibrary(player2, List.of(player2LibraryCard));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnByPlayer1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2HandCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(player2LibraryCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Failed Inspection");
        harness.assertInGraveyard(player2, "Failed Inspection");
    }
}
