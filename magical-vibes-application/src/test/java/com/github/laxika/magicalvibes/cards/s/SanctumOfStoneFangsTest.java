package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SanctumOfStoneFangsTest extends BaseCardTest {

    @Test
    @DisplayName("At your first main phase, each opponent loses life and you gain life for each Shrine you control")
    void drainsForEachShrineYouControl() {
        harness.addToBattlefield(player1, new SanctumOfStoneFangs());
        harness.addToBattlefield(player1, new SanctumOfTranquilLight());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's first main phase")
    void doesNotTriggerOnOpponentsFirstMainPhase() {
        harness.addToBattlefield(player1, new SanctumOfStoneFangs());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
