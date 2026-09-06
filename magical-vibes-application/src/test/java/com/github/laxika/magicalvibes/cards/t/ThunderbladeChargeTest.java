package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ThunderbladeCharge.class, GrizzlyBears.class})
class ThunderbladeChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a player when cast from hand")
    void dealsDamageWhenCastFromHand() {
        harness.setHand(player1, List.of(new ThunderbladeCharge()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Paying the graveyard trigger cost offers Thunderblade Charge for a free cast")
    void paysToCastFromGraveyard() {
        Card charge = putChargeInGraveyard();
        addReadyAttacker();
        harness.setLife(player2, 20);

        resolveCombatDamageToTrigger();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(charge);
    }

    @Test
    @DisplayName("Declining the graveyard trigger cost leaves Thunderblade Charge in the graveyard")
    void declinesGraveyardCast() {
        Card charge = putChargeInGraveyard();
        addReadyAttacker();
        harness.setLife(player2, 20);

        resolveCombatDamageToTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(charge);
    }

    @Test
    @DisplayName("One or more combat-damage dealers create only one graveyard trigger")
    void batchesCombatDamageTrigger() {
        putChargeInGraveyard();
        addReadyAttacker();
        addReadyAttacker();
        harness.setLife(player2, 20);

        resolveCombatDamageToTrigger();

        assertThat(gd.pendingMayAbilities).hasSize(1);
    }

    private Card putChargeInGraveyard() {
        Card charge = new ThunderbladeCharge();
        gd.playerGraveyards.get(player1.getId()).add(charge);
        return charge;
    }

    private void addReadyAttacker() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
    }

    private void resolveCombatDamageToTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
