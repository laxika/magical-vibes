package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CastleRaptors.class)
class CastleRaptorsTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +0/+2 while untapped")
    void getsToughnessBoostWhileUntapped() {
        Permanent raptors = harness.addToBattlefieldAndReturn(player1, new CastleRaptors());

        assertThat(gqs.getEffectivePower(gd, raptors)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raptors)).isEqualTo(5);
    }

    @Test
    @DisplayName("Loses the toughness boost while tapped")
    void losesToughnessBoostWhileTapped() {
        Permanent raptors = harness.addToBattlefieldAndReturn(player1, new CastleRaptors());
        raptors.tap();

        assertThat(gqs.getEffectivePower(gd, raptors)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raptors)).isEqualTo(3);
    }

    @Test
    @DisplayName("Regains the toughness boost when untapped")
    void regainsToughnessBoostWhenUntapped() {
        Permanent raptors = harness.addToBattlefieldAndReturn(player1, new CastleRaptors());
        raptors.tap();
        raptors.untap();

        assertThat(gqs.getEffectiveToughness(gd, raptors)).isEqualTo(5);
    }
}
