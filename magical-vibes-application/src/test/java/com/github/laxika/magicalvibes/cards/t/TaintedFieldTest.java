package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TaintedField.class, Swamp.class})
class TaintedFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds colorless mana")
    void tappingAddsColorlessMana() {
        Permanent field = addReadyField();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(field.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colored mana ability requires a Swamp")
    void coloredManaRequiresSwamp() {
        Permanent field = addReadyField();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Swamp");
        assertThat(field.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping adds white or black mana while controlling a Swamp")
    void tappingAddsColoredManaWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent field = addReadyField();

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(field.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping can add black mana while controlling a Swamp")
    void tappingAddsBlackManaWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent field = addReadyField();

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(field.isTapped()).isTrue();
    }

    private Permanent addReadyField() {
        Permanent field = harness.addToBattlefieldAndReturn(player1, new TaintedField());
        field.setSummoningSick(false);
        return field;
    }
}
