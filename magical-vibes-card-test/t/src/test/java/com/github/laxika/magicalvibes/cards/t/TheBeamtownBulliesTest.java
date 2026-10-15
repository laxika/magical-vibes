package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheBeamtownBullies.class, GrizzlyBears.class})
class TheBeamtownBulliesTest extends BaseCardTest {

    @Test
    void putsNonlegendaryCreatureUnderActiveOpponentsControlWithHasteAndGoad() {
        Permanent bullies = addCreatureReady(player1, new TheBeamtownBullies());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.forceActivePlayer(player2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), bears.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(als.getMustAttackRequirementCount(gd, returned)).isEqualTo(1);
        assertThat(gd.stolenCreatures).containsEntry(returned.getId(), player1.getId());
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(returned.getId(), DelayedPermanentActionKind.EXILE_AT_END_STEP));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(bullies.isTapped()).isTrue();
    }

    @Test
    void exilesReturnedCreatureAtNextEndStep() {
        addCreatureReady(player1, new TheBeamtownBullies());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.forceActivePlayer(player2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), bears.getId()));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    void cannotTargetOpponentWhenItIsNotTheirTurn() {
        addCreatureReady(player1, new TheBeamtownBullies());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent whose turn it is");
    }

    @Test
    void cannotTargetLegendaryCreatureCard() {
        addCreatureReady(player1, new TheBeamtownBullies());
        Card legendary = new TheBeamtownBullies();
        harness.setGraveyard(player1, List.of(legendary));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), legendary.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player2, "The Beamtown Bullies");
    }

    @Test
    void cannotTargetCreatureInOpponentsGraveyard() {
        addCreatureReady(player1, new TheBeamtownBullies());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetSelfDuringOwnTurn() {
        addCreatureReady(player1, new TheBeamtownBullies());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player1.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReturnCreatureThatLeftGraveyardBeforeResolution() {
        addCreatureReady(player1, new TheBeamtownBullies());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.forceActivePlayer(player2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), bears.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bears));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
    }

    @Test
    void resolvesAfterBulliesLeaveBattlefield() {
        addCreatureReady(player1, new TheBeamtownBullies());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.forceActivePlayer(player2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), bears.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(als.canAttack(gd, returned, player2.getId())).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, returned)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void delayedExileIsControlledByActivatingPlayer() {
        addCreatureReady(player1, new TheBeamtownBullies());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.forceActivePlayer(player2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), bears.getId()));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        StackEntry exileTrigger = gd.stack.getFirst();
        assertThat(exileTrigger.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    void delayedExileRetainsBulliesAsItsSource() {
        Permanent bullies = addCreatureReady(player1, new TheBeamtownBullies());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.forceActivePlayer(player2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), bears.getId()));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        StackEntry exileTrigger = gd.stack.getFirst();
        assertThat(exileTrigger.getCard().getId()).isEqualTo(bullies.getCard().getId());
    }

    @Test
    void activationDuringEndStepWaitsForNextTurnsEndStep() {
        addCreatureReady(player1, new TheBeamtownBullies());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), bears.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
    }

}
