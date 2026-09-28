package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GlimmeringAngel;
import com.github.laxika.magicalvibes.cards.h.HoodedKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanopySurge.class, GlimmeringAngel.class, HoodedKavu.class})
class CanopySurgeTest extends BaseCardTest {

    @Test
    void unkickedDealsOneDamageToPlayersAndFlyingCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent ownFlyer = harness.addToBattlefieldAndReturn(player1, new GlimmeringAngel());
        Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, new GlimmeringAngel());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new HoodedKavu());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new HoodedKavu());
        harness.setHand(player1, List.of(new CanopySurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(ownFlyer.getMarkedDamage()).isEqualTo(1);
        assertThat(opposingFlyer.getMarkedDamage()).isEqualTo(1);
        assertThat(ownBear.getMarkedDamage()).isZero();
        assertThat(opposingBear.getMarkedDamage()).isZero();
    }

    @Test
    void kickedDealsFourDamageToPlayersAndFlyingCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GlimmeringAngel());
        harness.addToBattlefield(player1, new HoodedKavu());
        harness.addToBattlefield(player2, new GlimmeringAngel());
        harness.addToBattlefield(player2, new HoodedKavu());
        harness.setHand(player1, List.of(new CanopySurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
        harness.assertNotOnBattlefield(player1, "Glimmering Angel");
        harness.assertNotOnBattlefield(player2, "Glimmering Angel");
        harness.assertOnBattlefield(player1, "Hooded Kavu");
        harness.assertOnBattlefield(player2, "Hooded Kavu");
    }
}
