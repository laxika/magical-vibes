package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RecklessAmplimancerTest extends BaseCardTest {

    @Test
    void activatingOnceDoublesPowerAndToughness() {
        Permanent amplimancer = addAmplimancerReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(amplimancer.getEffectivePower()).isEqualTo(4);
        assertThat(amplimancer.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void activatingTwiceCompoundsTheBoost() {
        Permanent amplimancer = addAmplimancerReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 10);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(amplimancer.getEffectivePower()).isEqualTo(8);
        assertThat(amplimancer.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent amplimancer = addAmplimancerReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(amplimancer.getEffectivePower()).isEqualTo(2);
        assertThat(amplimancer.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addAmplimancerReady(Player player) {
        GameData gameData = harness.getGameData();
        Permanent perm = new Permanent(new RecklessAmplimancer());
        perm.setSummoningSick(false);
        gameData.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
