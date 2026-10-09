package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.cards.c.CentaurHealer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrainpipeVermin.class, AxebaneStag.class, CentaurHealer.class})
class DrainpipeVerminTest extends BaseCardTest {

    // "When this creature dies, you may pay {B}. If you do, target player discards a card."

    /**
     * Puts Drainpipe Vermin on the battlefield blocking a lethal Axebane Stag attacker and advances to
     * combat damage so it dies, leaving the death trigger awaiting its target choice.
     */
    private void killInCombat() {
        Permanent vermin = harness.addToBattlefieldAndReturn(player1, new DrainpipeVermin());
        vermin.setSummoningSick(false);
        vermin.setBlocking(true);
        vermin.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new AxebaneStag());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // combat damage -> Drainpipe Vermin dies
    }

    @Test
    @DisplayName("Dies, target opponent chosen, pay {B} makes them discard a card")
    void diesPayTargetOpponentDiscards() {
        harness.setHand(player2, List.of(new AxebaneStag(), new CentaurHealer()));

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
        harness.setHand(player2, List.of(new AxebaneStag(), new CentaurHealer()));

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
        harness.setHand(player1, List.of(new AxebaneStag(), new CentaurHealer()));

        killInCombat();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Payment remains optional when the target has an empty hand")
    void canPayWithEmptyTargetHand() {
        harness.setHand(player2, List.of());

        killInCombat();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting without black mana does not cause a discard")
    void cannotPayWithBlueMana() {
        harness.setHand(player2, List.of(new AxebaneStag(), new CentaurHealer()));

        killInCombat();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.withAutoStop(gd.currentStep, () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
