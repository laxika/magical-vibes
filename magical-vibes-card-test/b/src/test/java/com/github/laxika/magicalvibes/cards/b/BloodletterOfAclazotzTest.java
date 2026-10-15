package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DeepCavernBat;
import com.github.laxika.magicalvibes.cards.e.EatenByPiranhas;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.Worship;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({BloodletterOfAclazotz.class, Shock.class, Worship.class, GrizzlyBears.class,
        DeepCavernBat.class, EatenByPiranhas.class})
class BloodletterOfAclazotzTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's life loss is doubled during your turn")
    void doublesOpponentLifeLossDuringYourTurn() {
        harness.addToBattlefield(player1, new BloodletterOfAclazotz());
        harness.forceActivePlayer(player1);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(
                gd, player2.getId(), 3, "test"));

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("The replacement does not affect your own life loss or an opponent's turn")
    void onlyAffectsOpponentsDuringYourTurn() {
        harness.addToBattlefield(player1, new BloodletterOfAclazotz());
        harness.forceActivePlayer(player1);
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(
                gd, player1.getId(), 3, "test"));

        harness.assertLife(player1, 17);

        harness.forceActivePlayer(player2);
        harness.setLife(player2, 20);
        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(
                gd, player2.getId(), 3, "test"));

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Damage still deals its original amount but causes doubled life loss")
    void damageCausesDoubledLifeLoss() {
        harness.addToBattlefield(player1, new BloodletterOfAclazotz());
        harness.forceActivePlayer(player1);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Worship allows its controller to survive by applying its replacement after Bloodletter")
    void worshipCanApplyAfterDoublingDamageCausedLifeLoss() {
        harness.addToBattlefield(player1, new BloodletterOfAclazotz());
        harness.addToBattlefield(player2, new Worship());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.setLife(player2, 2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 1);
    }

    @Test
    @DisplayName("Two Bloodletters multiply opponent life loss by four")
    void multipleCopiesStackMultiplicatively() {
        harness.addToBattlefield(player1, new BloodletterOfAclazotz());
        harness.addToBattlefield(player1, new BloodletterOfAclazotz());
        harness.forceActivePlayer(player1);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(
                gd, player2.getId(), 3, "test"));

        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("Paying life during the Bloodletter controller's turn causes doubled life loss")
    void doublesOpponentLifePayments() {
        harness.addToBattlefield(player1, new BloodletterOfAclazotz());
        harness.forceActivePlayer(player1);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifePayment(
                gd, player2.getId(), 2, "test"));

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A Bloodletter with no abilities does not replace life loss")
    void stopsDoublingWhenAbilitiesAreRemoved() {
        Permanent bloodletter = harness.addToBattlefieldAndReturn(player1, new BloodletterOfAclazotz());
        harness.forceActivePlayer(player1);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new EatenByPiranhas()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player2, 0, bloodletter.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(
                gd, player2.getId(), 3, "test"));

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Lifelink gains only the damage dealt while opponent life loss is doubled")
    void doesNotDoubleLifelinkGain() {
        harness.addToBattlefield(player1, new BloodletterOfAclazotz());
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new DeepCavernBat());
        bat.setSummoningSick(false);
        bat.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Setting an opponent's life total lower doubles the required life loss")
    void doublesLifeLossFromSettingLifeTotal() {
        harness.addToBattlefield(player1, new BloodletterOfAclazotz());
        harness.forceActivePlayer(player1);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applySetLifeTotal(
                gd, player2.getId(), 15));

        harness.assertLife(player2, 10);
    }
}
