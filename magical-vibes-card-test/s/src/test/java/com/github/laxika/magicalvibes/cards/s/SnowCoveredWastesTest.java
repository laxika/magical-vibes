package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnowCoveredWastes.class})
class SnowCoveredWastesTest extends BaseCardTest {

    @Test
    void tapsForOneColorlessSnowManaWithoutUsingTheStack() {
        Permanent wastes = harness.addToBattlefieldAndReturn(player1, new SnowCoveredWastes());

        harness.tapPermanent(player1, 0);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(wastes.isTapped()).isTrue();
        assertThat(pool.get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(pool.getTotalAllMana()).isEqualTo(1);
        assertThat(pool.getSnowMana(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void cannotProduceMoreManaWhileAlreadyTapped() {
        harness.addToBattlefield(player1, new SnowCoveredWastes());
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Permanent is already tapped");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getTotalAllMana()).isEqualTo(1);
        assertThat(pool.getSnowMana(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
