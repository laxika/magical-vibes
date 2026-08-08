package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeralAnimistTest extends BaseCardTest {

    @Test
    @DisplayName("Activating once doubles power and leaves toughness alone")
    void activatingOnceDoublesPower() {
        addAnimistReady(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent animist = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        // Base 2/1, X = 2 → +2/+0 → 4/1.
        assertThat(animist.getEffectivePower()).isEqualTo(4);
        assertThat(animist.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating twice snapshots the boosted power, growing to 8/1")
    void activatingTwiceCompounds() {
        addAnimistReady(player1);
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent animist = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        // After first: 4/1; second X = 4 → +4/+0 → 8/1.
        assertThat(animist.getEffectivePower()).isEqualTo(8);
        assertThat(animist.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addAnimistReady(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent animist = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(animist.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(animist.getEffectivePower()).isEqualTo(2);
        assertThat(animist.getEffectiveToughness()).isEqualTo(1);
    }

    private Permanent addAnimistReady(Player player) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(new FeralAnimist());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
