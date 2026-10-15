package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FlickeringSpirit;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CastleRaptors.class, FlickeringSpirit.class})
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

    @Test
    @DisplayName("Only boosts itself")
    void onlyBoostsItself() {
        Permanent raptors = harness.addToBattlefieldAndReturn(player1, new CastleRaptors());
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new FlickeringSpirit());

        assertThat(gqs.getEffectiveToughness(gd, raptors)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each copy checks its own tapped state without stacking bonuses")
    void copiesCheckTheirOwnTappedState() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CastleRaptors());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CastleRaptors());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new CastleRaptors());
        first.tap();

        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(5);

        first.untap();
        second.tap();
        opposing.tap();

        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(3);
    }

    @Test
    @DisplayName("The untap step restores the bonus only for the active player's Raptors")
    void untapStepRestoresBonusForActivePlayer() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new CastleRaptors());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new CastleRaptors());
        own.tap();
        opposing.tap();

        harness.performUntapStep(player1);

        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(3);
    }
}
