package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindSpirit;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodAgeGeneral.class, GrizzlyBears.class, WindSpirit.class})
class BloodAgeGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping boosts attacking Spirits but not other creatures")
    void tappingBoostsAttackingSpiritsOnly() {
        Permanent general = addCreatureReady(player1, new BloodAgeGeneral());
        Permanent spirit = addCreatureReady(player1, new WindSpirit());
        Permanent nonSpirit = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttackingSpirit = addCreatureReady(player1, new WindSpirit());
        int spiritPower = gqs.getEffectivePower(gd, spirit);
        int spiritToughness = gqs.getEffectiveToughness(gd, spirit);
        int nonSpiritPower = gqs.getEffectivePower(gd, nonSpirit);
        int nonAttackingSpiritPower = gqs.getEffectivePower(gd, nonAttackingSpirit);

        spirit.setAttacking(true);
        nonSpirit.setAttacking(true);
        harness.activateAbility(player1, indexOf(player1, general), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(spiritPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(spiritToughness);
        assertThat(gqs.getEffectivePower(gd, nonSpirit)).isEqualTo(nonSpiritPower);
        assertThat(gqs.getEffectivePower(gd, nonAttackingSpirit)).isEqualTo(nonAttackingSpiritPower);
    }

    @Test
    @DisplayName("Spirit boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent general = addCreatureReady(player1, new BloodAgeGeneral());
        Permanent spirit = addCreatureReady(player1, new WindSpirit());
        int spiritPower = gqs.getEffectivePower(gd, spirit);

        spirit.setAttacking(true);
        harness.activateAbility(player1, indexOf(player1, general), null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(spiritPower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(spiritPower);
    }

    @Test
    @DisplayName("Boost applies to attacking Spirits controlled by an opponent")
    void boostsOpponentsAttackingSpirits() {
        Permanent general = addCreatureReady(player1, new BloodAgeGeneral());
        Permanent spirit = addCreatureReady(player2, new WindSpirit());
        int spiritPower = gqs.getEffectivePower(gd, spirit);

        spirit.setAttacking(true);
        harness.activateAbility(player1, indexOf(player1, general), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(spiritPower + 1);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
