package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.g.Ghostfire;
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

@CardUsed({InterventionPact.class, BlindPhantasm.class, Ghostfire.class})
class InterventionPactTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the chosen source's next damage and gains that much life")
    void preventsDamageAndGainsLife() {
        harness.setLife(player1, 20);
        Permanent source = castInterventionPact();

        source.setAttacking(true);
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
    @DisplayName("Damage from a source other than the chosen one is not prevented")
    void doesNotPreventDamageFromDifferentSource() {
        harness.setLife(player1, 20);
        Permanent chosenSource = addCreatureReady(player2, new BlindPhantasm());
        Permanent otherSource = addCreatureReady(player2, new BlindPhantasm());
        castInterventionPactChoosing(chosenSource);

        otherSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prevents damage from a chosen spell on the stack and gains that much life")
    void preventsDamageFromChosenSpell() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Ghostfire ghostfire = new Ghostfire();
        harness.setHand(player2, List.of(ghostfire));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castFromHand(player1, new InterventionPact(), "{0}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ghostfire.getId());
        harness.handlePermanentChosen(player1, ghostfire.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
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
        Permanent source = addCreatureReady(player2, new BlindPhantasm());
        castInterventionPactChoosing(source);
        return source;
    }

    private void castInterventionPactChoosing(Permanent source) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new InterventionPact(), "{0}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
    }

    private void reachNextUpkeepPrompt() {
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.turnNumber = 3;
        gd.activePlayerId = player1.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();
    }

}
