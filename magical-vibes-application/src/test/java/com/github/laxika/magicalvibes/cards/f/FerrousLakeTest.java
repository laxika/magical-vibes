package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(FerrousLake.class)
class FerrousLakeTest extends BaseCardTest {

    @Test
    void payingOneGenericManaAddsBlueAndRed() {
        harness.addToBattlefield(player1, new FerrousLake());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        var pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityRequiresOneGenericMana() {
        Permanent lake = harness.addToBattlefieldAndReturn(player1, new FerrousLake());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(lake);
    }

    @Test
    void abilityRequiresSourceToBeUntapped() {
        harness.addToBattlefield(player1, new FerrousLake());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
