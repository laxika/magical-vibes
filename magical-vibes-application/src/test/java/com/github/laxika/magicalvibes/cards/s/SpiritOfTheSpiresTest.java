package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritOfTheSpires.class, SuntailHawk.class, GrizzlyBears.class})
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

    @Test
    void multipleSpiritsBoostEachOtherAndStackWithoutBoostingThemselves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SpiritOfTheSpires());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SpiritOfTheSpires());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new SpiritOfTheSpires());

        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(4);

        Permanent third = harness.addToBattlefieldAndReturn(player1, new SpiritOfTheSpires());

        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, third)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(4);
    }
}
