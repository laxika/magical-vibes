package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.p.Poxwalkers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Poxwalkers.class, NurglesRot.class})
class NurglesRotTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature dies, Nurgle's Rot returns to hand and creates a Plaguebearer of Nurgle")
    void returnsToHandAndCreatesPlaguebearerWhenEnchantedCreatureDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Poxwalkers());
        harness.setHand(player1, List.of(new NurglesRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        creature.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nurgle's Rot");
        harness.assertNotOnBattlefield(player1, "Nurgle's Rot");

        Permanent token = findPermanent(player1, "Plaguebearer of Nurgle");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nurgle's Rot can enchant only a creature an opponent controls")
    void cannotEnchantOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Poxwalkers());
        harness.setHand(player1, List.of(new NurglesRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The token is still created if Nurgle's Rot is exiled before its trigger resolves")
    void createsTokenEvenIfAuraLeavesGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Poxwalkers());
        NurglesRot aura = new NurglesRot();
        harness.setHand(player1, List.of(aura));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        creature.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Nurgle's Rot");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removeCardFromGraveyardByIdForExile(gd, aura.getId()));
        harness.setExile(player1, List.of(aura));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Nurgle's Rot");
        harness.assertOnBattlefield(player1, "Plaguebearer of Nurgle");
        harness.assertNotOnBattlefield(player2, "Plaguebearer of Nurgle");
    }

    @Test
    @DisplayName("The original trigger cannot return Nurgle's Rot after it leaves and reenters the graveyard")
    void doesNotReturnAuraFromLaterGraveyardEntry() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Poxwalkers());
        NurglesRot aura = new NurglesRot();
        harness.setHand(player1, List.of(aura));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        creature.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Nurgle's Rot");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removeCardFromGraveyardById(gd, aura.getId()));
        harness.addToBattlefield(player1, aura);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nurgle's Rot");
        harness.assertNotInHand(player1, "Nurgle's Rot");
        harness.assertOnBattlefield(player1, "Plaguebearer of Nurgle");
    }

    @Test
    @DisplayName("Bouncing the enchanted creature does not trigger Nurgle's Rot")
    void doesNotTriggerWhenEnchantedCreatureReturnsToHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Poxwalkers());
        harness.setHand(player1, List.of(new NurglesRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, creature));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nurgle's Rot");
        harness.assertNotInHand(player1, "Nurgle's Rot");
        harness.assertNotOnBattlefield(player1, "Plaguebearer of Nurgle");
    }

    @Test
    @DisplayName("The death of an unrelated creature does not trigger Nurgle's Rot")
    void doesNotTriggerWhenAnotherCreatureDies() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new Poxwalkers());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new Poxwalkers());
        harness.setHand(player1, List.of(new NurglesRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        other.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nurgle's Rot");
        harness.assertNotInHand(player1, "Nurgle's Rot");
        harness.assertNotOnBattlefield(player1, "Plaguebearer of Nurgle");
    }
}
