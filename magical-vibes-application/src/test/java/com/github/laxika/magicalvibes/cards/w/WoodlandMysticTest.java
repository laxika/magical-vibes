package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoodlandMystic.class})
class WoodlandMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Woodland Mystic produces one green mana")
    void tappingProducesGreenMana() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new WoodlandMystic());
        perm.setSummoningSick(false);

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Summoning-sick Woodland Mystic cannot tap for mana")
    void summoningSickCannotTap() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new WoodlandMystic());
        perm.setSummoningSick(true);

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Woodland Mystic cannot produce mana again")
    void cannotTapAgainWhileTapped() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new WoodlandMystic());
        perm.setSummoningSick(false);
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(perm.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Woodland Mystic adds mana to its controller's pool")
    void addsManaToItsController() {
        Permanent perm = harness.addToBattlefieldAndReturn(player2, new WoodlandMystic());
        perm.setSummoningSick(false);

        harness.tapPermanent(player2, 0);

        assertThat(perm.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
