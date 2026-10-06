package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
import com.github.laxika.magicalvibes.cards.c.ChangelingSentinel;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PreeminentCaptain.class, BallyrushBanneret.class, ChangelingSentinel.class,
        ElvishWarrior.class, JaceBeleren.class})
class PreeminentCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts a Soldier from hand onto the battlefield tapped and attacking")
    void putsSoldierTappedAndAttacking() {
        harness.setHand(player1, List.of(new BallyrushBanneret()));
        attackWithCaptain();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        Permanent soldier = findPermanent(player1, "Ballyrush Banneret");
        assertThat(soldier).isNotNull();
        assertThat(soldier.isTapped()).isTrue();
        assertThat(soldier.isAttacking()).isTrue();
        assertThat(soldier.isAttackedThisTurn()).isFalse();
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

    @Test
    @DisplayName("A changeling enters tapped and attacking despite vigilance")
    void changelingEntersTappedDespiteVigilance() {
        harness.setHand(player1, List.of(new ChangelingSentinel()));
        attackWithCaptain();

        harness.handleCardChosen(player1, 0);

        Permanent sentinel = findPermanent(player1, "Changeling Sentinel");
        assertThat(sentinel.isTapped()).isTrue();
        assertThat(sentinel.isAttacking()).isTrue();
        assertThat(sentinel.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Captain entering attacking does not trigger its attack ability")
    void enteringCaptainDoesNotTriggerAttackAbility() {
        harness.setHand(player1, List.of(new PreeminentCaptain(), new BallyrushBanneret()));
        attackWithCaptain();

        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Preeminent Captain")).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Ballyrush Banneret");
    }

    @Test
    @DisplayName("The entering Soldier's attack destination is chosen independently of the Captain")
    void offersChoiceOfAttackDestination() {
        harness.addToBattlefield(player2, new JaceBeleren());
        harness.setHand(player1, List.of(new BallyrushBanneret()));
        attackWithCaptain();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal lethal damage to the Captain")
    void firstStrikeKillsBlockerBeforeNormalDamage() {
        harness.setHand(player1, List.of());
        addCreatureReady(player1, new PreeminentCaptain());
        harness.addToBattlefield(player2, new BallyrushBanneret());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Preeminent Captain");
        harness.assertInGraveyard(player2, "Ballyrush Banneret");
        harness.assertLife(player2, 20);
    }
}
