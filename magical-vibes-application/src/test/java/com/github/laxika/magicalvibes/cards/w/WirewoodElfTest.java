package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(WirewoodElf.class)
class WirewoodElfTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Wirewood Elf produces one green mana")
    void tappingProducesGreenMana() {
        Permanent elf = addCreatureReady(player1, new WirewoodElf());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(elf.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning-sick Wirewood Elf cannot tap for mana")
    void summoningSickCannotTap() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new WirewoodElf());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(elf.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Wirewood Elf cannot produce mana again")
    void tappedCannotTapAgain() {
        Permanent elf = addCreatureReady(player1, new WirewoodElf());

        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(elf.isTapped()).isTrue();
    }
}
