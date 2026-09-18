package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FireElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GauntletOfMight.class, FireElemental.class, Forest.class, GrizzlyBears.class, Mountain.class})
class GauntletOfMightTest extends BaseCardTest {

    @Test
    void redCreaturesGetPlusOnePlusOneRegardlessOfController() {
        harness.addToBattlefield(player1, new GauntletOfMight());
        Permanent ownRedCreature = harness.addToBattlefieldAndReturn(player1, new FireElemental());
        Permanent opponentRedCreature = harness.addToBattlefieldAndReturn(player2, new FireElemental());
        Permanent ownNonRedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentNonRedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownRedCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownRedCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opponentRedCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, opponentRedCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownNonRedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownNonRedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentNonRedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentNonRedCreature)).isEqualTo(2);
    }

    @Test
    void mountainTappingAddsAdditionalRedManaToItsController() {
        harness.addToBattlefield(player1, new GauntletOfMight());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void tappingNonMountainLandDoesNotAddRedMana() {
        harness.addToBattlefield(player1, new GauntletOfMight());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
