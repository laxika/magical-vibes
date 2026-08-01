package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DrainpipeVerminTest extends BaseCardTest {

    // "When this creature dies, you may pay {B}. If you do, target player discards a card."

    /**
     * Puts Drainpipe Vermin on the battlefield blocking a lethal 5/5 attacker and advances to
     * combat damage so it dies, leaving the death trigger awaiting its target choice.
     */
    private void killInCombat() {
        Permanent vermin = new Permanent(new DrainpipeVermin());
        vermin.setSummoningSick(false);
        vermin.setBlocking(true);
        vermin.addBlockingTarget(0);
        gd.playerBattlefields.get(player1.getId()).add(vermin);

        GrizzlyBears bearsCard = new GrizzlyBears();
        bearsCard.setPower(5);
        bearsCard.setToughness(5);
        Permanent attacker = new Permanent(bearsCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // combat damage -> Drainpipe Vermin dies
    }

    @Test
    @DisplayName("Dies, target opponent chosen, pay {B} makes them discard a card")
    void diesPayTargetOpponentDiscards() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));

        killInCombat();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve the death trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the {B} payment makes no one discard")
    void declinePaymentNoDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));

        killInCombat();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The controller may target themselves")
    void mayTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));

        killInCombat();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
