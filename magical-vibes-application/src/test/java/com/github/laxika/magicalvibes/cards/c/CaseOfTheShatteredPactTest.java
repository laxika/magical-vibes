package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinMaskmaker;
import com.github.laxika.magicalvibes.cards.j.JadedAnalyst;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheGuildpact;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.cards.r.RubblebeltMaverick;
import com.github.laxika.magicalvibes.cards.u.UnauthorizedExit;
import com.github.laxika.magicalvibes.cards.u.UnscrupulousAgent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaseOfTheShatteredPact.class, Forest.class, RubblebeltMaverick.class, NoviceInspector.class,
        JadedAnalyst.class, UnscrupulousAgent.class, GoblinMaskmaker.class,
        LeylineOfTheGuildpact.class, UnauthorizedExit.class})
class CaseOfTheShatteredPactTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land when it enters")
    void searchesForBasicLand() {
        harness.setHand(player1, List.of(new CaseOfTheShatteredPact()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Solves with permanents of all five colors and grants the solved ability")
    void solvesWithAllFiveColors() {
        harness.addToBattlefield(player1, new CaseOfTheShatteredPact());
        addFiveColors();
        Permanent creature = addCreatureReady(player1, new RubblebeltMaverick());

        solveAtEndStep();

        harness.forceActivePlayer(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Does not solve until all five colors are among permanents controlled")
    void doesNotSolveWithOnlyFourColors() {
        harness.addToBattlefield(player1, new CaseOfTheShatteredPact());
        harness.addToBattlefield(player1, new NoviceInspector());
        harness.addToBattlefield(player1, new JadedAnalyst());
        harness.addToBattlefield(player1, new UnscrupulousAgent());
        Permanent creature = addCreatureReady(player1, new RubblebeltMaverick());

        solveAtEndStep();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canFailToFindEvenWhenBasicLandIsAvailable() {
        harness.setHand(player1, List.of(new CaseOfTheShatteredPact()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void oneMulticoloredPermanentCanSupplyAllFiveColors() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheShatteredPact());
        harness.addToBattlefield(player1, new LeylineOfTheGuildpact());

        solveAtEndStep();

        assertThat(casePermanent.isSolved()).isTrue();
    }

    @Test
    void opposingPermanentsDoNotSupplyMissingColors() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheShatteredPact());
        harness.addToBattlefield(player1, new NoviceInspector());
        harness.addToBattlefield(player1, new JadedAnalyst());
        harness.addToBattlefield(player1, new UnscrupulousAgent());
        harness.addToBattlefield(player1, new RubblebeltMaverick());
        harness.addToBattlefield(player2, new GoblinMaskmaker());
        harness.addToBattlefield(player1, new Forest());

        solveAtEndStep();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    void doesNotSolveDuringOpponentsEndStep() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheShatteredPact());
        addFiveColors();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    void losingAColorBeforeSolveResolvesPreventsSolving() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheShatteredPact());
        addFiveColors();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        bounce(harness.getPermanentId(player1, "Goblin Maskmaker"));
        harness.passBothPriorities();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    void solvedAbilityPersistsAfterLosingColorsAndExpiresAtCleanup() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheShatteredPact());
        Permanent leyline = harness.addToBattlefieldAndReturn(player1, new LeylineOfTheGuildpact());
        Permanent creature = addCreatureReady(player1, new RubblebeltMaverick());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        solveAtEndStep();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP, () -> bounce(leyline.getId()));
        assertThat(casePermanent.isSolved()).isTrue();
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void combatTriggerResolvesAfterSolvedCaseLeavesBattlefield() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheShatteredPact());
        addFiveColors();
        Permanent creature = addCreatureReady(player1, new RubblebeltMaverick());
        solveAtEndStep();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(gd.stack).hasSize(1);

        bounce(casePermanent.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Case of the Shattered Pact");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void nonbasicCardsCannotBeFoundByTheSearch() {
        harness.setHand(player1, List.of(new CaseOfTheShatteredPact()));
        harness.setLibrary(player1, List.of(new RubblebeltMaverick()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void gainingMissingColorAfterEndStepBeginsDoesNotSolve() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheShatteredPact());
        harness.addToBattlefield(player1, new NoviceInspector());
        harness.addToBattlefield(player1, new JadedAnalyst());
        harness.addToBattlefield(player1, new UnscrupulousAgent());
        harness.addToBattlefield(player1, new RubblebeltMaverick());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        harness.addToBattlefield(player1, new GoblinMaskmaker());
        harness.passBothPriorities();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    void solvedAbilityDoesNotTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new CaseOfTheShatteredPact());
        addFiveColors();
        Permanent creature = addCreatureReady(player1, new RubblebeltMaverick());
        solveAtEndStep();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    private void addFiveColors() {
        harness.addToBattlefield(player1, new NoviceInspector());
        harness.addToBattlefield(player1, new JadedAnalyst());
        harness.addToBattlefield(player1, new UnscrupulousAgent());
        harness.addToBattlefield(player1, new GoblinMaskmaker());
        harness.addToBattlefield(player1, new RubblebeltMaverick());
    }

    private void solveAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private void bounce(UUID permanentId) {
        harness.setHand(player2, List.of(new UnauthorizedExit()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, permanentId);
        harness.passBothPriorities();
    }
}
