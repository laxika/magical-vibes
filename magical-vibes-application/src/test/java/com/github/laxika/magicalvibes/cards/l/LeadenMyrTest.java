package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(LeadenMyr.class)
class LeadenMyrTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Leaden Myr produces one black mana")
    void tappingProducesBlackMana() {
        Permanent perm = addCreatureReady(player1, new LeadenMyr());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning sick Leaden Myr cannot be tapped for mana")
    void summoningSickCannotTap() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new LeadenMyr());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(perm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Leaden Myr cannot be tapped for mana again")
    void tappedCannotTapAgain() {
        Permanent perm = addCreatureReady(player1, new LeadenMyr());

        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(perm.isTapped()).isTrue();
    }
}
