package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadbridgeShaman.class, Murder.class, GrizzlyBears.class, Peek.class})
class DeadbridgeShamanTest extends BaseCardTest {

    // "When this creature dies, target opponent discards a card."

    private void startMainPhaseWithMurder() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, new ArrayList<>(List.of(new Murder())));
        harness.addMana(player1, ManaColor.BLACK, 3);
    }

    @Test
    @DisplayName("Dying makes the target opponent discard a card")
    void deathTriggerDiscards() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new DeadbridgeShaman());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        startMainPhaseWithMurder();

        harness.castAndResolveInstant(player1, 0, shaman.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Empty opponent hand: nothing is discarded")
    void emptyHandDiscardsNothing() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new DeadbridgeShaman());
        harness.setHand(player2, new ArrayList<>());
        startMainPhaseWithMurder();

        harness.castAndResolveInstant(player1, 0, shaman.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Shaman staying on the battlefield does not trigger the discard")
    void noDeathNoDiscard() {
        harness.addToBattlefieldAndReturn(player1, new DeadbridgeShaman());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        startMainPhaseWithMurder();

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The dying Shaman's controller cannot target themselves")
    void cannotTargetController() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new DeadbridgeShaman());
        harness.setHand(player2, List.of(new DeadbridgeShaman(), new DeadbridgeShaman()));
        startMainPhaseWithMurder();
        harness.castAndResolveInstant(player1, 0, shaman.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent-controlled Shaman makes the other player discard")
    void opponentControlledDeathTargetsOtherPlayer() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player2, new DeadbridgeShaman());
        startMainPhaseWithMurder();
        harness.setHand(player1, List.of(new Murder(), new DeadbridgeShaman(), new DeadbridgeShaman()));
        harness.setHand(player2, List.of(new DeadbridgeShaman(), new DeadbridgeShaman()));

        harness.castAndResolveInstant(player1, 0, shaman.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Deadbridge Shaman");
        harness.assertInGraveyard(player2, "Deadbridge Shaman");
    }

    @Test
    @CardUsed(DeadbridgeShaman.class)
    @DisplayName("Lethal damage triggers a chosen discard without a destroy spell")
    void lethalDamageTriggersDiscard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new DeadbridgeShaman());
        DeadbridgeShaman first = new DeadbridgeShaman();
        DeadbridgeShaman second = new DeadbridgeShaman();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(first, second));
        shaman.setMarkedDamage(1);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(second);
        harness.assertInGraveyard(player1, "Deadbridge Shaman");
    }
}
