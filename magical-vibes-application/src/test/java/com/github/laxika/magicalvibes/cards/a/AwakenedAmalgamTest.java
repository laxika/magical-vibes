package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AwakenedAmalgamTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of differently named lands you control")
    void powerAndToughnessEqualDifferentlyNamedControlledLands() {
        Permanent amalgam = harness.addToBattlefieldAndReturn(player1, new AwakenedAmalgam());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, amalgam)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, amalgam)).isEqualTo(3);
    }

    @Test
    @DisplayName("P/T updates when the set of differently named lands changes")
    void powerAndToughnessUpdateWhenLandNamesChange() {
        Permanent amalgam = harness.addToBattlefieldAndReturn(player1, new AwakenedAmalgam());

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, amalgam)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, amalgam)).isEqualTo(1);

        harness.addToBattlefield(player1, new Island());
        assertThat(gqs.getEffectivePower(gd, amalgam)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, amalgam)).isEqualTo(2);
    }
}
