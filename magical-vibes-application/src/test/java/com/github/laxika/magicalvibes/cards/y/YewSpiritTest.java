package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class YewSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability once adds X = current power to both stats")
    void activatingOnceDoublesStats() {
        addSpiritReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        // Base 3/3, X = 3 → +3/+3 → 6/6.
        assertThat(spirit.getEffectivePower()).isEqualTo(6);
        assertThat(spirit.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Activating twice snapshots the boosted power, growing to 12/12")
    void activatingTwiceCompounds() {
        addSpiritReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        // After first: 6/6; second X = 6 → +6/+6 → 12/12.
        assertThat(spirit.getEffectivePower()).isEqualTo(12);
        assertThat(spirit.getEffectiveToughness()).isEqualTo(12);
    }

    @Test
    @DisplayName("The boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addSpiritReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent spirit = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(spirit.getEffectivePower()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spirit.getEffectivePower()).isEqualTo(3);
        assertThat(spirit.getEffectiveToughness()).isEqualTo(3);
    }

    private Permanent addSpiritReady(Player player) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(new YewSpirit());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
