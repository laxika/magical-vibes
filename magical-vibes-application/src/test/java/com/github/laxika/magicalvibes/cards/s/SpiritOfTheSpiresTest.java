package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpiritOfTheSpiresTest extends BaseCardTest {

    @Test
    void boostsOtherFlyingCreaturesYouControl() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SpiritOfTheSpires());
        Permanent ownFlyer = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent ownGroundCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        assertThat(gqs.getEffectiveToughness(gd, ownFlyer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownGroundCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingFlyer)).isEqualTo(1);
    }

    @Test
    void boostEndsWhenSpiritLeavesTheBattlefield() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SpiritOfTheSpires());
        Permanent ownFlyer = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());

        assertThat(gqs.getEffectiveToughness(gd, ownFlyer)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(spirit);

        assertThat(gqs.getEffectiveToughness(gd, ownFlyer)).isEqualTo(1);
    }
}
