package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreathWeapon.class, GrizzlyBears.class, HillGiant.class, Mountain.class, ShivanDragon.class})
class BreathWeaponTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to non-Dragon creatures and leaves Dragons unharmed")
    void damagesOnlyNonDragonCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        harness.castFromHand(player1, new BreathWeapon(), "{2}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        assertThat(dragon.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Shivan Dragon");
    }

    @Test
    @DisplayName("Resolves with only Dragons and lands without damaging either player")
    void resolvesWithoutNonDragonCreatures() {
        Permanent ownDragon = harness.addToBattlefieldAndReturn(player1, new ShivanDragon());
        Permanent opponentDragon = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setLife(player1, 17);
        harness.setLife(player2, 13);

        harness.castFromHand(player1, new BreathWeapon(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(ownDragon.getMarkedDamage()).isZero();
        assertThat(opponentDragon.getMarkedDamage()).isZero();
        assertThat(mountain.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Shivan Dragon");
        harness.assertOnBattlefield(player2, "Shivan Dragon");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 13);
        harness.assertInGraveyard(player1, "Breath Weapon");
        assertThat(gd.stack).isEmpty();
    }
}
