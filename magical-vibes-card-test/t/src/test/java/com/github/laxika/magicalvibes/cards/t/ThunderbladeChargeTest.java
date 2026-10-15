package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrinningIgnus;
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

@CardUsed({ThunderbladeCharge.class, GrinningIgnus.class})
class ThunderbladeChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a player when cast from hand")
    void dealsDamageWhenCastFromHand() {
        harness.setHand(player1, List.of(new ThunderbladeCharge()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

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
    @DisplayName("Declining the free-cast choice leaves Thunderblade Charge in the graveyard")
    void declinesFreeCast() {
        Card charge = putChargeInGraveyard();
        addReadyAttacker();
        harness.setLife(player2, 20);

        resolveCombatDamageToTrigger();
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(charge);
    }

    @Test
    @DisplayName("The graveyard trigger does nothing if Thunderblade Charge leaves the graveyard first")
    void doesNotResolveAfterLeavingGraveyard() {
        Card charge = putChargeInGraveyard();
        addReadyAttacker();
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        gd.playerGraveyards.get(player1.getId()).remove(charge);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
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

    @Test
    @DisplayName("Deals lethal damage to a creature")
    void dealsDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrinningIgnus());
        harness.setHand(player1, List.of(new ThunderbladeCharge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grinning Ignus");
        harness.assertInGraveyard(player2, "Grinning Ignus");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent's combat damage does not trigger the graveyard ability")
    void doesNotTriggerForOpponentCreature() {
        Card charge = putChargeInGraveyard();
        Permanent attacker = addCreatureReady(player2, new GrinningIgnus());
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(charge);
    }

    @Test
    @DisplayName("Leaving and reentering the graveyard invalidates the original trigger")
    void doesNotResolveAfterLeavingAndReenteringGraveyard() {
        Card charge = putChargeInGraveyard();
        addReadyAttacker();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        gd.playerGraveyards.get(player1.getId()).remove(charge);
        harness.setHand(player1, List.of(charge));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(charge));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(charge);
    }

    private Card putChargeInGraveyard() {
        Card charge = new ThunderbladeCharge();
        harness.setGraveyard(player1, List.of(charge));
        return charge;
    }

    private void addReadyAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrinningIgnus());
        attacker.setAttacking(true);
    }

    private void resolveCombatDamageToTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
