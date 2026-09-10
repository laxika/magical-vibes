package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.VenerableMonk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoltariChampion.class, VenerableMonk.class})
class SoltariChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts other creatures you control, but not itself or opponents' creatures")
    void attackBoostsOtherOwnCreatures() {
        Permanent champion = addCreatureReady(player1, new SoltariChampion());
        Permanent monk = addCreatureReady(player1, new VenerableMonk());
        Permanent enemyMonk = addCreatureReady(player2, new VenerableMonk());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, enemyMonk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyMonk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Shadow creatures can block or be blocked only by creatures with shadow")
    void shadowRestrictsBlockingBothWays() {
        Permanent champion = addCreatureReady(player1, new SoltariChampion());
        Permanent monk = addCreatureReady(player2, new VenerableMonk());

        assertThat(bls.canBlockAttacker(gd, monk, champion,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, champion, monk,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Attack boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new SoltariChampion());
        Permanent monk = addCreatureReady(player1, new VenerableMonk());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
    }
}
