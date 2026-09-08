package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThranQuarryTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds the chosen color")
    void manaAbilityAddsChosenColor() {
        Permanent quarry = harness.addToBattlefieldAndReturn(player1, new ThranQuarry());
        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(before + 1);
        assertThat(quarry.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifices itself at the beginning of any end step when its controller controls no creatures")
    void sacrificesAtEndStepWithNoCreatures() {
        harness.addToBattlefield(player1, new ThranQuarry());

        advanceToEndStep(player2);

        harness.assertNotOnBattlefield(player1, "Thran Quarry");
        harness.assertInGraveyard(player1, "Thran Quarry");
    }

    @Test
    @DisplayName("Survives the end step while its controller controls a creature")
    void survivesWithCreature() {
        harness.addToBattlefield(player1, new ThranQuarry());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToEndStep(player2);

        harness.assertOnBattlefield(player1, "Thran Quarry");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's creature does not satisfy the condition")
    void opponentCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new ThranQuarry());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player2);

        harness.assertNotOnBattlefield(player1, "Thran Quarry");
        harness.assertInGraveyard(player1, "Thran Quarry");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
