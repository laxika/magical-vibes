package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PowerBoost.class, GrizzlyBears.class})
class PowerBoostTest extends BaseCardTest {

    @Test
    void boostsCreaturesYouControlByOnePower() {
        harness.addToBattlefield(player1, new PowerBoost());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    void bonusDisappearsWhenPowerBoostLeavesBattlefield() {
        Permanent powerBoost = harness.addToBattlefieldAndReturn(player1, new PowerBoost());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(powerBoost);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }
}
