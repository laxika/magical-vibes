package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.Worship;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({BloodletterOfAclazotz.class, Shock.class, Worship.class, GrizzlyBears.class})
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

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A damage life floor is applied before damage-caused life loss is doubled")
    void appliesDamageLifeFloorBeforeDoublingDamageCausedLifeLoss() {
        harness.addToBattlefield(player1, new BloodletterOfAclazotz());
        harness.addToBattlefield(player2, new Worship());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.setLife(player2, 2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 0);
    }
}
