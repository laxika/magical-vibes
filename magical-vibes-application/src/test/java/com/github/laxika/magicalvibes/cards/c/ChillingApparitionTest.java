package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChillingApparitionTest extends BaseCardTest {

    @Test
    @DisplayName("Regeneration ability grants a regeneration shield")
    void regeneratesThisCreature() {
        Permanent apparition = harness.addToBattlefieldAndReturn(player1, new ChillingApparition());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(apparition.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to a player makes that player discard a card")
    void combatDamageMakesPlayerDiscard() {
        Permanent apparition = addReadyApparition();
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        apparition.setAttacking(true);

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage trigger does not happen when the creature is blocked")
    void blockedCombatDamageDoesNotTrigger() {
        Permanent apparition = addReadyApparition();
        apparition.setAttacking(true);
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private Permanent addReadyApparition() {
        Permanent apparition = harness.addToBattlefieldAndReturn(player1, new ChillingApparition());
        apparition.setSummoningSick(false);
        return apparition;
    }

    private void resolveCombatAndTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
