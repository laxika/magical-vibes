package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ZookeeperMechan.class)
class ZookeeperMechanTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Zookeeper Mechan adds red mana")
    void tapsForRedMana() {
        Permanent mechan = addCreatureReady(player1, new ZookeeperMechan());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(mechan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability gives a creature you control +4/+0 until end of turn")
    void boostsControlledCreature() {
        addCreatureReady(player1, new ZookeeperMechan());
        Permanent target = addCreatureReady(player1, new ZookeeperMechan());
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Second ability cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        addCreatureReady(player1, new ZookeeperMechan());
        Permanent target = addCreatureReady(player2, new ZookeeperMechan());
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(7);
    }
}
