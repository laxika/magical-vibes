package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BishopsSoldier;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PreeminentCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts a Soldier from hand onto the battlefield tapped and attacking")
    void putsSoldierTappedAndAttacking() {
        harness.setHand(player1, List.of(new BishopsSoldier()));
        attackWithCaptain();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent soldier = findPermanent(player1, "Bishop's Soldier");
        assertThat(soldier).isNotNull();
        assertThat(soldier.isTapped()).isTrue();
        assertThat(soldier.isAttackedThisTurn()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only Soldier creature cards in hand are offered")
    void offersOnlySoldierCreatures() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new BishopsSoldier()));
        attackWithCaptain();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Declining leaves the Soldier in hand")
    void decliningLeavesSoldierInHand() {
        harness.setHand(player1, List.of(new BishopsSoldier()));
        attackWithCaptain();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Bishop's Soldier"));
    }

    @Test
    @DisplayName("No Soldier in hand — no choice is prompted")
    void noSoldierInHandDoesNothing() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        attackWithCaptain();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void attackWithCaptain() {
        Permanent captain = new Permanent(new PreeminentCaptain());
        captain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(captain);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve the attack trigger
        harness.passBothPriorities();
    }
}
