package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DomriRade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlateStreetRuffian.class, GrizzlyBears.class, Forest.class, DomriRade.class})
class SlateStreetRuffianTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked makes the defending player discard a card")
    void blockedForcesDefendingPlayerDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        addAttackingRuffian();
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // The defending player, not the Ruffian's controller, chooses the discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An unblocked Ruffian causes no discard")
    void unblockedNoDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        addAttackingRuffian();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller's hand is untouched when the Ruffian is blocked")
    void controllerDoesNotDiscard() {
        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        addAttackingRuffian();
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Multiple blockers cause only one discard")
    void multipleBlockersCauseOneDiscard() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
        addAttackingRuffian();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty defending hand does not prevent the trigger resolving")
    void emptyDefendingHand() {
        harness.setHand(player2, List.of());
        addAttackingRuffian();
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Blocking a Ruffian attacking a planeswalker makes its controller discard")
    void attackingPlaneswalkerMakesDefendingPlayerDiscard() {
        Permanent domri = harness.addToBattlefieldAndReturn(player2, new DomriRade());
        domri.setCounterCount(CounterType.LOYALTY, 3);
        Permanent ruffian = addCreatureReady(player1, new SlateStreetRuffian());
        ruffian.setAttacking(true);
        ruffian.setAttackTarget(domri.getId());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Defending player still discards if the attacked planeswalker leaves before resolution")
    void attackedPlaneswalkerLeavingDoesNotPreventDiscard() {
        Permanent domri = harness.addToBattlefieldAndReturn(player2, new DomriRade());
        domri.setCounterCount(CounterType.LOYALTY, 3);
        Permanent ruffian = addCreatureReady(player1, new SlateStreetRuffian());
        ruffian.setAttacking(true);
        ruffian.setAttackTarget(domri.getId());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Forest()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        assertThat(gd.stack).hasSize(1);

        domri.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Domri Rade");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
    }
    private void addAttackingRuffian() {
        Permanent perm = addCreatureReady(player1, new SlateStreetRuffian());
        perm.setAttacking(true);
        perm.setAttackTarget(player2.getId());
    }
}
