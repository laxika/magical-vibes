package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BenalishSleeper;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SolKanarTheTainted.class, BenalishSleeper.class, LilianaOfTheVeil.class})
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
        harness.passUntil(player1, TurnStep.END_STEP);
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
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BenalishSleeper());

        beginEndStep();
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, bears.getId())).isNull();
        assertThat(gqs.findPermanentById(gd, solKanar.getId())).isNotNull();
    }

    @Test
    void damageModeCanTargetAPlaneswalker() {
        addSolKanar();
        Permanent liliana = harness.enterBattlefieldAndReturn(player2, new LilianaOfTheVeil());

        beginEndStep();
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, liliana.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, liliana.getId())).isNull();
        harness.assertInGraveyard(player2, "Liliana of the Veil");
    }

    @Test
    void damageModeCanTargetAnotherCreatureYouControl() {
        Permanent solKanar = addSolKanar();
        Permanent sleeper = harness.addToBattlefieldAndReturn(player1, new BenalishSleeper());

        beginEndStep();
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, sleeper.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, sleeper.getId())).isNull();
        assertThat(gqs.findPermanentById(gd, solKanar.getId())).isNotNull();
    }

    @Test
    void dealsExactlyThreeDamageToAnotherSolKanar() {
        Permanent source = addSolKanar();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SolKanarTheTainted());

        beginEndStep();
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, target.getId())).isNotNull();
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(source.getMarkedDamage()).isZero();
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
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BenalishSleeper());

        beginEndStep();
        harness.handleListChoice(player1, DAMAGE);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, solKanar.getId())).isNotNull();
        assertThat(gqs.findPermanentById(gd, bears.getId())).isNotNull();
    }

    @Test
    void damageModeCanBeChosenWithoutAnotherPermanentAndIsStillConsumed() {
        Permanent solKanar = addSolKanar();

        beginEndStep();
        harness.handleListChoice(player1, DAMAGE);
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, solKanar.getId())).isNotNull();

        beginEndStep();
        assertThatThrownBy(() -> harness.handleListChoice(player1, DAMAGE))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, DRAIN);
        harness.passBothPriorities();
    }

    @Test
    void eachModeCanBeChosenOnlyOnce() {
        addSolKanar();

        beginEndStep();
        harness.handleListChoice(player1, DRAW);
        harness.passBothPriorities();

        beginEndStep();

        assertThatThrownBy(() -> harness.handleListChoice(player1, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, DRAIN);
        harness.passBothPriorities();
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

    @Test
    void exileResetsModesAndNewControllerReceivesTheirBenefits() {
        Permanent original = addSolKanar();

        beginEndStep();
        harness.handleListChoice(player1, DRAIN);
        harness.passBothPriorities();

        beginEndStep();
        harness.handleListChoice(player1, EXILE);
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, original.getId())).isNull();
        harness.assertOnBattlefield(player2, "Sol'Kanar the Tainted");
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.handleListChoice(player2, DRAIN);
        harness.passBothPriorities();

        harness.assertLife(player1, player1Life - 2);
        harness.assertLife(player2, player2Life + 2);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.handleListChoice(player2, DRAW);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        addSolKanar();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
