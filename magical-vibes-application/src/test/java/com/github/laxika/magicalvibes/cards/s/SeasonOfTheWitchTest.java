package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfGranite;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonOfTheWitch.class, GrizzlyBears.class, WallOfGranite.class})
class SeasonOfTheWitchTest extends BaseCardTest {

    @Test
    void destroysUntappedCreaturesThatCouldAttack() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());
        Permanent doomed = addCreatureReady(player2, new GrizzlyBears());
        Permanent wall = addCreatureReady(player2, new WallOfGranite());
        Permanent attacked = addCreatureReady(player2, new GrizzlyBears());
        attacked.setAttackedThisTurn(true);
        Permanent otherPlayerCreature = addCreatureReady(player1, new GrizzlyBears());

        runEndStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(doomed)
                .contains(wall, attacked);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherPlayerCreature);
    }

    @Test
    void paysTwoLifeToKeepTheEnchantment() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        harness.assertOnBattlefield(player1, "Season of the Witch");
    }

    @Test
    void sacrificesTheEnchantmentWhenLifePaymentIsDeclined() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());

        advanceToUpkeep(player1);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Season of the Witch");
        harness.assertInGraveyard(player1, "Season of the Witch");
    }

    private void runEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(StepTriggerService.class).handleEndStepTriggers(gd));
        harness.passBothPriorities();
    }
}
