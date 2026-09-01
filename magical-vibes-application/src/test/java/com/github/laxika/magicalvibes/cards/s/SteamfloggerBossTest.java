package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoriokRigger;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteamfloggerBoss.class, MoriokRigger.class, GrizzlyBears.class})
class SteamfloggerBossTest extends BaseCardTest {

    @Test
    void boostsOtherRiggersYouControlWithHaste() {
        Permanent rigger = harness.addToBattlefieldAndReturn(player1, new MoriokRigger());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingRigger = harness.addToBattlefieldAndReturn(player2, new MoriokRigger());

        int riggerPower = gqs.getEffectivePower(gd, rigger);
        int bearPower = gqs.getEffectivePower(gd, bear);
        int opposingRiggerPower = gqs.getEffectivePower(gd, opposingRigger);
        Permanent boss = harness.addToBattlefieldAndReturn(player1, new SteamfloggerBoss());
           assertThat(gqs.getEffectivePower(gd, boss)).isEqualTo(boss.getCard().getPower());
        assertThat(gqs.getEffectivePower(gd, rigger)).isEqualTo(riggerPower + 1);
        assertThat(gqs.hasKeyword(gd, rigger, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(bearPower);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposingRigger)).isEqualTo(opposingRiggerPower);
        assertThat(gqs.hasKeyword(gd, opposingRigger, Keyword.HASTE)).isFalse();
    }
}
