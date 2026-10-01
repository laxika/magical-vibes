package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
import com.github.laxika.magicalvibes.cards.c.ChangelingSentinel;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PreeminentCaptain.class, BallyrushBanneret.class, ChangelingSentinel.class, ElvishWarrior.class})
class PreeminentCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts a Soldier from hand onto the battlefield tapped and attacking")
    void putsSoldierTappedAndAttacking() {
        harness.setHand(player1, List.of(new BallyrushBanneret()));
        attackWithCaptainAtPlayer2();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent soldier = findPermanent(player1, "Ballyrush Banneret");
        assertThat(soldier).isNotNull();
        assertThat(soldier.isTapped()).isTrue();
        assertThat(soldier.isAttackedThisTurn()).isTrue();
        assertThat(soldier.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only Soldier creature cards in hand are offered")
    void offersOnlySoldierCreatures() {
        harness.setHand(player1, List.of(new ElvishWarrior(), new BallyrushBanneret(), new ChangelingSentinel()));
        attackWithCaptain();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("Declining leaves the Soldier in hand")
    void decliningLeavesSoldierInHand() {
        harness.setHand(player1, List.of(new BallyrushBanneret()));
        attackWithCaptain();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Ballyrush Banneret");
    }

    @Test
    @DisplayName("No Soldier in hand — no choice is prompted")
    void noSoldierInHandDoesNothing() {
        harness.setHand(player1, List.of(new ElvishWarrior()));
        attackWithCaptain();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void attackWithCaptain() {
        addCreatureReady(player1, new PreeminentCaptain());
        declareAttackers(List.of(0));
        resolveAllTriggers();
    }

    private void attackWithCaptainAtPlayer2() {
        addCreatureReady(player1, new PreeminentCaptain());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, player2.getId()));
        resolveAllTriggers();
    }
}
