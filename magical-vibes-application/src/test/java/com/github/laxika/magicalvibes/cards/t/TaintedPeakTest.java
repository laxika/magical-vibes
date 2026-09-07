package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TaintedPeak.class, Swamp.class})
class TaintedPeakTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana produces one colorless")
    void tappingForColorlessMana() {
        Permanent peak = addReadyPeak(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(peak.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colored mana ability requires a Swamp")
    void coloredManaRequiresSwamp() {
        Permanent peak = addReadyPeak(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("a Swamp");
        assertThat(peak.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Colored mana ability can produce black mana")
    void coloredManaCanProduceBlack() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent peak = addReadyPeak(player1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(peak.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colored mana ability can produce red mana")
    void coloredManaCanProduceRed() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent peak = addReadyPeak(player1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(peak.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's Swamp does not enable colored mana")
    void opponentsSwampDoesNotEnableColoredMana() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent peak = addReadyPeak(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(peak.isTapped()).isFalse();
    }

    private Permanent addReadyPeak(Player player) {
        Permanent perm = new Permanent(new TaintedPeak());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
