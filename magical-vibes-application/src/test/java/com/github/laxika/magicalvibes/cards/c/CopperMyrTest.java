package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CopperMyr.class)
class CopperMyrTest extends BaseCardTest {

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping Copper Myr produces one green mana")
    void tappingProducesGreenMana() {
        Permanent perm = addCreatureReady(player1, new CopperMyr());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning sick Copper Myr cannot be tapped for mana")
    void summoningSickCannotTap() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(perm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Copper Myr cannot be tapped for mana again")
    void tappedCannotTapAgain() {
        Permanent perm = addCreatureReady(player1, new CopperMyr());

        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
    }
}
