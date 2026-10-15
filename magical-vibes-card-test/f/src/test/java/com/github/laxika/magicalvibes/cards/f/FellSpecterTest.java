package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FellSpecter.class, Distress.class, GrizzlyBears.class, MindRot.class, GreenwoodSentinel.class})
class FellSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB trigger makes the target opponent discard, and the discard trigger drains 2 life")
    void etbDiscardAlsoDrainsTwoLife() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);
        castFellSpecter(player2.getId());

        harness.passBothPriorities(); // creature spell
        harness.passBothPriorities(); // ETB trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // life-loss trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("No discard and no life loss when the target opponent's hand is empty")
    void emptyHandDoesNothing() {
        harness.setHand(player2, new ArrayList<>());
        harness.setLife(player2, 20);
        castFellSpecter(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent discard from another source also drains 2 life")
    void triggersOnDiscardFromAnotherSource() {
        harness.addToBattlefield(player1, new FellSpecter());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Controller's own discard does not trigger the life loss")
    void controllerDiscardDoesNotTrigger() {
        harness.addToBattlefield(player2, new FellSpecter());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target yourself with the ETB trigger")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new FellSpecter()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Discarding two cards creates two independently resolving life-loss triggers")
    void multipleDiscardsCreateSeparateTriggers() {
        harness.addToBattlefield(player1, new FellSpecter());
        harness.setHand(player2, List.of(
                new GreenwoodSentinel(), new GreenwoodSentinel(), new GreenwoodSentinel()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
    }

    private void castFellSpecter(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new FellSpecter()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, targetPlayerId);
    }
}
