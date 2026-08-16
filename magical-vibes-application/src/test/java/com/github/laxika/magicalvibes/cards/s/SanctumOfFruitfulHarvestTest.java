package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SanctumOfFruitfulHarvestTest extends BaseCardTest {

    @Test
    @DisplayName("At your first main phase, adds one mana of one color for each Shrine you control")
    void addsManaForEachShrineYouControl() {
        harness.addToBattlefield(player1, new SanctumOfFruitfulHarvest());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's first main phase")
    void doesNotTriggerOnOpponentsFirstMainPhase() {
        harness.addToBattlefield(player1, new SanctumOfFruitfulHarvest());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
