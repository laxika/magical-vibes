package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CyberConversion;
import com.github.laxika.magicalvibes.cards.w.WordsOfWorship;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSecondDoctor.class, GrizzlyBears.class, WordsOfWorship.class, CyberConversion.class})
class TheSecondDoctorTest extends BaseCardTest {

    @Test
    @DisplayName("Players have no maximum hand size")
    void playersHaveNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new TheSecondDoctor());
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears())));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("An opponent who draws cannot attack the Doctor during their next turn")
    void opponentWhoDrawsCannotAttackDuringNextTurn() {
        harness.addToBattlefield(player1, new TheSecondDoctor());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handleXValueChosen(player1, 1);
            harness.handleXValueChosen(player2, 1);
        });

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(als.canAttackDefender(
                gd,
                gd.playerBattlefields.get(player2.getId()).get(0),
                player1.getId())).isFalse();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(als.canAttackDefender(
                gd,
                gd.playerBattlefields.get(player2.getId()).get(0),
                player1.getId())).isFalse();
    }

    @Test
    void opponentMayDeclineWhileControllerDraws() {
        prepareDrawChoices();
        var attacker = addCreatureReady(player2, new GrizzlyBears());
        beginEndStepChoices();

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handleXValueChosen(player1, 1);
            harness.handleXValueChosen(player2, 0);
        });

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(als.canAttackDefender(gd, attacker, player1.getId())).isTrue();
    }

    @Test
    void controllerMayDeclineWhileOpponentDraws() {
        prepareDrawChoices();
        var attacker = addCreatureReady(player2, new GrizzlyBears());
        beginEndStepChoices();

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handleXValueChosen(player1, 0);
            harness.handleXValueChosen(player2, 1);
        });

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(als.canAttackDefender(gd, attacker, player1.getId())).isFalse();
    }

    @Test
    void acceptingReplacedDrawStillRestrictsAttacking() {
        prepareDrawChoices();
        harness.addToBattlefield(player2, new WordsOfWorship());
        var attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            harness.activateAbility(player2, 0, null, null);
            resolveAllTriggers();
        });
        beginEndStepChoices();

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handleXValueChosen(player1, 0);
            harness.handleXValueChosen(player2, 1);
        });

        harness.assertLife(player2, 25);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(als.canAttackDefender(gd, attacker, player1.getId())).isFalse();
    }

    @Test
    void restrictionSurvivesSourceLeavingAndExpiresAfterOpponentsNextTurn() {
        prepareDrawChoices();
        var attacker = addCreatureReady(player2, new GrizzlyBears());
        beginEndStepChoices();
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handleXValueChosen(player1, 0);
            harness.handleXValueChosen(player2, 1);
        });
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(als.canAttackDefender(gd, attacker, player1.getId())).isFalse();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(als.canAttackDefender(gd, attacker, player1.getId())).isTrue();
    }

    @Test
    void opponentAlsoHasNoMaximumHandSize() {
        harness.addToBattlefield(player1, new TheSecondDoctor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, IntStream.range(0, 9)
                .mapToObj(i -> (Card) new GrizzlyBears()).toList());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(9);
    }

    @Test
    void faceDownDoctorDoesNotPreventCleanupDiscard() {
        harness.addToBattlefield(player1, new TheSecondDoctor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new CyberConversion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () ->
                harness.castAndResolveInstant(player1, 0,
                        gd.playerBattlefields.get(player1.getId()).getFirst().getId()));
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, IntStream.range(0, 9)
                .mapToObj(i -> (Card) new GrizzlyBears()).toList());

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
    }

    @Test
    void doesNotOfferDrawsDuringOpponentsEndStep() {
        prepareDrawChoices();
        harness.forceActivePlayer(player2);

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void prepareDrawChoices() {
        harness.addToBattlefield(player1, new TheSecondDoctor());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void beginEndStepChoices() {
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
    }
}
