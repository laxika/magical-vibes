package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlingshotGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a target blue creature")
    void dealsDamageToBlueCreature() {
        Permanent goblin = addReadyGoblin(player1);
        Permanent target = addCreatureReady(player2, new AzureDrake());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(goblin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonblue creature")
    void cannotTargetNonblueCreature() {
        addReadyGoblin(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyGoblin(Player player) {
        return addCreatureReady(player, new SlingshotGoblin());
    }
}
