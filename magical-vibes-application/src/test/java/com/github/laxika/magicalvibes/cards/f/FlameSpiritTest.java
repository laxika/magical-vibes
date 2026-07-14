package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlameSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: gives +1/+0 until end of turn")
    void firebreathingBoosts() {
        Permanent flameSpirit = addReadyFlameSpirit(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve ability

        assertThat(flameSpirit.getPowerModifier()).isEqualTo(1);
        assertThat(flameSpirit.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating the ability multiple times stacks")
    void firebreathingStacks() {
        Permanent flameSpirit = addReadyFlameSpirit(player1);

        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(flameSpirit.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent flameSpirit = addReadyFlameSpirit(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(flameSpirit.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(flameSpirit.getPowerModifier()).isEqualTo(0);
    }

    private Permanent addReadyFlameSpirit(Player player) {
        Permanent perm = new Permanent(new FlameSpirit());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
