package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThranQuarry.class, CoralMerfolk.class})
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
        harness.addToBattlefield(player1, new CoralMerfolk());

        advanceToEndStep(player2);

        harness.assertOnBattlefield(player1, "Thran Quarry");
        harness.assertOnBattlefield(player1, "Coral Merfolk");
    }

    @Test
    @DisplayName("An opponent's creature does not satisfy the condition")
    void opponentCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new ThranQuarry());
        harness.addToBattlefield(player2, new CoralMerfolk());

        advanceToEndStep(player2);

        harness.assertNotOnBattlefield(player1, "Thran Quarry");
        harness.assertInGraveyard(player1, "Thran Quarry");
        harness.assertOnBattlefield(player2, "Coral Merfolk");
    }

    @Test
    @DisplayName("Also sacrifices itself at its controller's end step when its controller controls no creatures")
    void sacrificesAtControllersEndStepWithNoCreatures() {
        harness.addToBattlefield(player1, new ThranQuarry());

        advanceToEndStep(player1);

        harness.assertNotOnBattlefield(player1, "Thran Quarry");
        harness.assertInGraveyard(player1, "Thran Quarry");
    }

    @Test
    @DisplayName("Does not sacrifice itself if a creature enters before its trigger resolves")
    void doesNotSacrificeIfCreatureEntersBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new ThranQuarry());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.addToBattlefield(player1, new CoralMerfolk());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thran Quarry");
        harness.assertNotInGraveyard(player1, "Thran Quarry");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
