package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SkyshipStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability gives Skyship Stalker +1/+0 until end of turn")
    void boostsSelf() {
        Permanent stalker = addStalkerReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(stalker.getPowerModifier()).isEqualTo(1);
        assertThat(stalker.getToughnessModifier()).isEqualTo(0);

        endTurn();

        assertThat(stalker.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The second ability grants first strike until end of turn")
    void grantsFirstStrike() {
        Permanent stalker = addStalkerReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.FIRST_STRIKE)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The third ability grants haste until end of turn")
    void grantsHaste() {
        Permanent stalker = addStalkerReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.HASTE)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.HASTE)).isFalse();
    }

    private Permanent addStalkerReady(Player player) {
        return addCreatureReady(player, new SkyshipStalker());
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
