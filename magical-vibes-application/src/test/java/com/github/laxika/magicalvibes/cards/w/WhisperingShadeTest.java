package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WhisperingShadeTest extends BaseCardTest {

    @Test
    @DisplayName("{B} gives Whispering Shade +1/+1 until end of turn")
    void blackManaAbilityBoostsShade() {
        Permanent shade = addReadyShade(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(2);
    }

    @Test
    @DisplayName("Whispering Shade's boosts stack")
    void boostsStack() {
        Permanent shade = addReadyShade(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(3);
    }

    @Test
    @DisplayName("Whispering Shade's boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent shade = addReadyShade(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(1);
    }

    private Permanent addReadyShade(Player player) {
        Permanent perm = new Permanent(new WhisperingShade());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
