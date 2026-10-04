package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoobbueGardener.class})
class GoobbueGardenerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Goobbue Gardener produces one green mana")
    void tappingProducesGreenMana() {
        Permanent gardener = harness.addToBattlefieldAndReturn(player1, new GoobbueGardener());
        gardener.setSummoningSick(false);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gardener.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("A summoning-sick Goobbue Gardener cannot produce mana")
    void summoningSickCannotTapForMana() {
        Permanent gardener = harness.addToBattlefieldAndReturn(player1, new GoobbueGardener());
        gardener.setSummoningSick(true);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(gardener.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("An already tapped Goobbue Gardener cannot produce more mana")
    void cannotActivateAgainWhileTapped() {
        Permanent gardener = harness.addToBattlefieldAndReturn(player1, new GoobbueGardener());
        gardener.setSummoningSick(false);
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
