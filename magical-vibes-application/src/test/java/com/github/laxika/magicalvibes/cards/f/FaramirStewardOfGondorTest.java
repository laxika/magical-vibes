package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BeregondOfTheGuard;
import com.github.laxika.magicalvibes.cards.s.StonehewerGiant;
import com.github.laxika.magicalvibes.cards.a.ArwenWeaverOfHope;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaramirStewardOfGondor.class, BeregondOfTheGuard.class, StonehewerGiant.class, ArwenWeaverOfHope.class})
class FaramirStewardOfGondorTest extends BaseCardTest {

    @Test
    void becomesMonarchWhenAControlledLegendaryCreatureWithManaValueFourOrGreaterEnters() {
        harness.addToBattlefield(player1, new FaramirStewardOfGondor());

        harness.enterBattlefieldAndReturn(player1, new BeregondOfTheGuard());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void doesNotTriggerForALegendaryCreatureWithManaValueLessThanFour() {
        harness.addToBattlefield(player1, new FaramirStewardOfGondor());

        harness.enterBattlefieldAndReturn(player1, new ArwenWeaverOfHope());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void createsTwoHumanSoldierTokensAtTheMonarchsEndStep() {
        harness.addToBattlefield(player1, new FaramirStewardOfGondor());
        gd.monarchPlayerId = player1.getId();

        advanceToEndStep(player1);

        assertThat(countPermanents(player1, "Human Soldier")).isEqualTo(2);
    }

    @Test
    void doesNotCreateTokensAtTheEndStepWhenItsControllerIsNotTheMonarch() {
        harness.addToBattlefield(player1, new FaramirStewardOfGondor());
        gd.monarchPlayerId = player2.getId();

        advanceToEndStep(player1);

        assertThat(countPermanents(player1, "Human Soldier")).isZero();
    }

    @Test
    void doesNotBecomeMonarchWhenAnOpponentsLegendaryCreatureEnters() {
        harness.addToBattlefield(player1, new FaramirStewardOfGondor());
        harness.enterBattlefieldAndReturn(player2, new BeregondOfTheGuard());
        resolveAllTriggers();
        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void doesNotBecomeMonarchWhenANonlegendaryCreatureWithManaValueFiveEnters() {
        harness.addToBattlefield(player1, new FaramirStewardOfGondor());
        harness.enterBattlefieldAndReturn(player1, new StonehewerGiant());
        resolveAllTriggers();
        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    void takesMonarchFromAnOpponentWhenAQualifyingCreatureEnters() {
        harness.addToBattlefield(player1, new FaramirStewardOfGondor());
        gd.monarchPlayerId = player2.getId();
        harness.enterBattlefieldAndReturn(player1, new BeregondOfTheGuard());
        resolveAllTriggers();
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void doesNotCreateTokensDuringTheOpponentsEndStep() {
        harness.addToBattlefield(player1, new FaramirStewardOfGondor());
        gd.monarchPlayerId = player1.getId();
        advanceToEndStep(player2);
        assertThat(countPermanents(player1, "Human Soldier")).isZero();
    }

    @Test
    void doesNotCreateTokensIfItsControllerLosesMonarchBeforeResolution() {
        harness.addToBattlefield(player1, new FaramirStewardOfGondor());
        gd.monarchPlayerId = player1.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isNotEmpty();
        gd.monarchPlayerId = player2.getId();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Human Soldier")).isZero();
    }

    @Test
    void becomingMonarchAfterTheEndStepBeginsDoesNotCreateTokens() {
        harness.addToBattlefield(player1, new FaramirStewardOfGondor());
        gd.monarchPlayerId = player2.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        gd.monarchPlayerId = player1.getId();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Human Soldier")).isZero();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
