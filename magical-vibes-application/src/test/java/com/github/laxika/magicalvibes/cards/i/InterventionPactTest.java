package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PayManaOrLoseGameAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InterventionPact.class, GoblinPiker.class})
class InterventionPactTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the chosen source's next damage and gains that much life")
    void preventsDamageAndGainsLife() {
        harness.setLife(player1, 20);
        Permanent goblin = castInterventionPact();

        goblin.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 22);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Schedules the next-upkeep payment")
    void schedulesNextUpkeepPayment() {
        castInterventionPact();

        List<PayManaOrLoseGameAtNextUpkeep> scheduled =
                gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class);
        assertThat(scheduled).singleElement()
                .satisfies(action -> {
                    assertThat(action.playerId()).isEqualTo(player1.getId());
                    assertThat(action.manaCost()).isEqualTo("{1}{W}{W}");
                });
    }

    @Test
    @DisplayName("Paying at the next upkeep avoids losing the game")
    void payingAtNextUpkeepAvoidsLoss() {
        castInterventionPact();
        reachNextUpkeepPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.getDelayedActions(PayManaOrLoseGameAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Declining at the next upkeep loses the game")
    void decliningAtNextUpkeepCausesLoss() {
        castInterventionPact();
        reachNextUpkeepPrompt();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private Permanent castInterventionPact() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent goblin = addReadyGoblin(player2);
        harness.setHand(player1, List.of(new InterventionPact()));
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());
        return goblin;
    }

    private void reachNextUpkeepPrompt() {
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.turnNumber = 3;
        gd.activePlayerId = player1.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();
    }

    private Permanent addReadyGoblin(com.github.laxika.magicalvibes.model.Player player) {
        Permanent goblin = new Permanent(new GoblinPiker());
        goblin.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(goblin);
        return goblin;
    }
}
