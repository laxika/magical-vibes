package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SolKanarTheTainted.class, GrizzlyBears.class})
class SolKanarTheTaintedTest extends BaseCardTest {

    private static final String DRAW = "Draw a card";
    private static final String DRAIN = "Each opponent loses 2 life and you gain 2 life";
    private static final String DAMAGE =
            "Sol'Kanar deals 3 damage to up to one other target creature or planeswalker";
    private static final String EXILE =
            "Exile Sol'Kanar, then return it to the battlefield under an opponent's control";

    private Permanent addSolKanar() {
        return harness.addToBattlefieldAndReturn(player1, new SolKanarTheTainted());
    }

    private void beginEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void drawsACard() {
        addSolKanar();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        beginEndStep();
        harness.handleListChoice(player1, DRAW);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void drainsEachOpponentAndGainsLife() {
        addSolKanar();
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        beginEndStep();
        harness.handleListChoice(player1, DRAIN);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    void dealsThreeDamageToAnotherCreature() {
        Permanent solKanar = addSolKanar();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        beginEndStep();
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, bears.getId())).isNull();
        assertThat(gqs.findPermanentById(gd, solKanar.getId())).isNotNull();
    }

    @Test
    void damageModeCannotTargetSolKanar() {
        Permanent solKanar = addSolKanar();

        beginEndStep();
        harness.handleListChoice(player1, DAMAGE);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, solKanar.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageModeMayDeclineItsOptionalTarget() {
        Permanent solKanar = addSolKanar();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        beginEndStep();
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, solKanar.getId())).isNotNull();
        assertThat(gqs.findPermanentById(gd, bears.getId())).isNotNull();
    }

    @Test
    void eachModeCanBeChosenOnlyOnce() {
        addSolKanar();

        beginEndStep();
        harness.handleListChoice(player1, DRAW);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(() -> {
            stepTriggerService().handleEndStepTriggers(gd);
            harness.getTriggerCollectionService().processNextTriggeredModalTrigger(gd);
        });
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, DRAIN);
        harness.passBothPriorities();
    }

    private StepTriggerService stepTriggerService() {
        return GameTestEngineContext.get().getBean(StepTriggerService.class);
    }

    @Test
    void exileModeReturnsSolKanarUnderOpponentsControl() {
        Permanent solKanar = addSolKanar();

        beginEndStep();
        harness.handleListChoice(player1, EXILE);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(solKanar.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Sol'Kanar the Tainted"));
    }
}
