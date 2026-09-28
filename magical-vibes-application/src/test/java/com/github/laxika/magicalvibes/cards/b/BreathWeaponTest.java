package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreathWeapon.class, GrizzlyBears.class, HillGiant.class, ShivanDragon.class})
class BreathWeaponTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to non-Dragon creatures and leaves Dragons unharmed")
    void damagesOnlyNonDragonCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        harness.setHand(player1, List.of(new BreathWeapon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        assertThat(dragon.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Shivan Dragon");
    }
}
