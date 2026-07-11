package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChameleonColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability once doubles power and toughness (X = current power)")
    void activatingOnceDoublesStats() {
        addColossusReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent colossus = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        // Base 4/4, X = 4 → +4/+4 → 8/8.
        assertThat(colossus.getEffectivePower()).isEqualTo(8);
        assertThat(colossus.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Activating twice snapshots the boosted power, doubling again to 16/16")
    void activatingTwiceCompounds() {
        addColossusReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent colossus = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        // After first: 8/8; second X = 8 → +8/+8 → 16/16.
        assertThat(colossus.getEffectivePower()).isEqualTo(16);
        assertThat(colossus.getEffectiveToughness()).isEqualTo(16);
    }

    @Test
    @DisplayName("The boost wears off at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addColossusReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent colossus = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(colossus.getEffectivePower()).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(colossus.getEffectivePower()).isEqualTo(4);
        assertThat(colossus.getEffectiveToughness()).isEqualTo(4);
    }

    private Permanent addColossusReady(Player player) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(new ChameleonColossus());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
