package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DesolateMire.class)
class DesolateMireTest extends BaseCardTest {

    @Test
    @DisplayName("Paying one and tapping Desolate Mire adds white and black mana")
    void addsWhiteAndBlackMana() {
        Permanent mire = addReadyMire();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(mire.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Cannot activate Desolate Mire without paying one")
    void cannotActivateWithoutMana() {
        addReadyMire();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate Desolate Mire while tapped")
    void cannotActivateWhileTapped() {
        Permanent mire = addReadyMire();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        mire.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addReadyMire() {
        Permanent mire = new Permanent(new DesolateMire());
        mire.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mire);
        return mire;
    }
}
