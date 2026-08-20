package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindSpirit;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodAgeGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping boosts attacking Spirits but not other creatures")
    void tappingBoostsAttackingSpiritsOnly() {
        Permanent general = addCreatureReady(player1, new BloodAgeGeneral());
        Permanent spirit = addCreatureReady(player1, new WindSpirit());
        Permanent nonSpirit = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttackingSpirit = addCreatureReady(player1, new WindSpirit());
        int spiritPower = spirit.getEffectivePower();
        int spiritToughness = spirit.getEffectiveToughness();
        int nonSpiritPower = nonSpirit.getEffectivePower();
        int nonAttackingSpiritPower = nonAttackingSpirit.getEffectivePower();

        declareAttackers(player1, List.of(indexOf(player1, spirit), indexOf(player1, nonSpirit)));
        harness.activateAbility(player1, indexOf(player1, general), null, null);
        harness.passBothPriorities();

        assertThat(spirit.getEffectivePower()).isEqualTo(spiritPower + 1);
        assertThat(spirit.getEffectiveToughness()).isEqualTo(spiritToughness);
        assertThat(nonSpirit.getEffectivePower()).isEqualTo(nonSpiritPower);
        assertThat(nonAttackingSpirit.getEffectivePower()).isEqualTo(nonAttackingSpiritPower);
    }

    @Test
    @DisplayName("Spirit boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent general = addCreatureReady(player1, new BloodAgeGeneral());
        Permanent spirit = addCreatureReady(player1, new WindSpirit());
        int spiritPower = spirit.getEffectivePower();

        declareAttackers(player1, List.of(indexOf(player1, spirit)));
        harness.activateAbility(player1, indexOf(player1, general), null, null);
        harness.passBothPriorities();
        assertThat(spirit.getEffectivePower()).isEqualTo(spiritPower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spirit.getEffectivePower()).isEqualTo(spiritPower);
    }

    @Test
    @DisplayName("Boost applies to attacking Spirits controlled by an opponent")
    void boostsOpponentsAttackingSpirits() {
        Permanent general = addCreatureReady(player1, new BloodAgeGeneral());
        Permanent spirit = addCreatureReady(player2, new WindSpirit());
        int spiritPower = spirit.getEffectivePower();

        declareAttackers(player2, List.of(indexOf(player2, spirit)));
        harness.activateAbility(player1, indexOf(player1, general), null, null);
        harness.passBothPriorities();

        assertThat(spirit.getEffectivePower()).isEqualTo(spiritPower + 1);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
