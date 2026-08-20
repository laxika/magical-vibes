package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoordinatedManeuverTest extends BaseCardTest {

    @Test
    void dealsDamageEqualToCreaturesControlled() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent target = new Permanent(new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 6);
        gd.playerBattlefields.get(player2.getId()).add(target);

        cast(0, target);

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void destroysTargetEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(1, target);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    void eachModeRejectsAnIllegalTarget() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        assertThatThrownBy(() -> cast(0, enchantment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(1, creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchantment");
    }

    private void cast(int mode, Permanent target) {
        harness.setHand(player1, List.of(new CoordinatedManeuver()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, mode, target.getId());
        harness.passBothPriorities();
    }
}
