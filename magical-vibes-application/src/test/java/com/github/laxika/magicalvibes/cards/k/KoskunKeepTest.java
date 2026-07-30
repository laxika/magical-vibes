package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KoskunKeepTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: adds {C}")
    void tapsForColorless() {
        Permanent keep = addKoskunKeep();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mana(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(keep.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{1}, {T}: spends the generic mana and adds {R}")
    void tapsForRed() {
        Permanent keep = addKoskunKeep();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(mana(ManaColor.RED)).isEqualTo(1);
        assertThat(mana(ManaColor.COLORLESS)).isZero();
        assertThat(keep.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{2}, {T}: adds {B}")
    void tapsForBlack() {
        addKoskunKeep();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(mana(ManaColor.BLACK)).isEqualTo(1);
        assertThat(mana(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("{2}, {T}: adds {G}")
    void tapsForGreen() {
        addKoskunKeep();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 3, null, null);

        assertThat(mana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(mana(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The {1} ability cannot be activated without the mana to pay for it")
    void cannotActivateRedAbilityWithoutMana() {
        Permanent keep = addKoskunKeep();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mana(ManaColor.RED)).isZero();
        assertThat(keep.isTapped()).isFalse();
    }

    private Permanent addKoskunKeep() {
        Permanent keep = harness.addToBattlefieldAndReturn(player1, new KoskunKeep());
        keep.setSummoningSick(false);
        return keep;
    }

    private int mana(ManaColor color) {
        return gd.playerManaPools.get(player1.getId()).get(color);
    }
}
