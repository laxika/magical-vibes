package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WyllPactBoundDuelist;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheHourglassCoven.class, WyllPactBoundDuelist.class, GrizzlyBears.class})
class TheHourglassCovenTest extends BaseCardTest {

    @Test
    void buffsOtherWarlocksYouControlOnly() {
        Permanent ownWarlock = harness.addToBattlefieldAndReturn(player1, new WyllPactBoundDuelist());
        Permanent ownNonWarlock = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingWarlock = harness.addToBattlefieldAndReturn(player2, new WyllPactBoundDuelist());

        int ownWarlockPower = gqs.getEffectivePower(gd, ownWarlock);
        int ownWarlockToughness = gqs.getEffectiveToughness(gd, ownWarlock);
        int ownNonWarlockPower = gqs.getEffectivePower(gd, ownNonWarlock);
        int ownNonWarlockToughness = gqs.getEffectiveToughness(gd, ownNonWarlock);
        int opposingWarlockPower = gqs.getEffectivePower(gd, opposingWarlock);
        int opposingWarlockToughness = gqs.getEffectiveToughness(gd, opposingWarlock);

        harness.addToBattlefield(player1, new TheHourglassCoven());

        assertThat(gqs.getEffectivePower(gd, ownWarlock)).isEqualTo(ownWarlockPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownWarlock)).isEqualTo(ownWarlockToughness + 1);
        assertThat(gqs.getEffectivePower(gd, ownNonWarlock)).isEqualTo(ownNonWarlockPower);
        assertThat(gqs.getEffectiveToughness(gd, ownNonWarlock)).isEqualTo(ownNonWarlockToughness);
        assertThat(gqs.getEffectivePower(gd, opposingWarlock)).isEqualTo(opposingWarlockPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingWarlock)).isEqualTo(opposingWarlockToughness);
    }
}
