package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JohanTest extends BaseCardTest {

    private void resolveCombatMay(boolean accepted) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accepted);
    }

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    @Test
    void acceptingMayLetsOtherCreaturesAttackWithoutTapping() {
        Permanent johan = addCreatureReady(player1, new Johan());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        resolveCombatMay(true);
        declareAttackers(List.of(1));

        assertThat(johan.isTapped()).isFalse();
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    void acceptingMayPreventsJohanFromAttacking() {
        Permanent johan = addCreatureReady(player1, new Johan());

        resolveCombatMay(true);
        beginAttackers();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(johan.isAttacking()).isFalse();
    }

    @Test
    void decliningMayLetsJohanAttackAndTap() {
        Permanent johan = addCreatureReady(player1, new Johan());

        resolveCombatMay(false);
        declareAttackers(List.of(0));

        assertThat(johan.isTapped()).isTrue();
    }

    @Test
    void tappedJohanDoesNotPreventAttackersFromTapping() {
        Permanent johan = addCreatureReady(player1, new Johan());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        resolveCombatMay(true);
        johan.tap();
        declareAttackers(List.of(1));

        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    void combatPermissionExpiresAtEndOfCombat() {
        addCreatureReady(player1, new Johan());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        resolveCombatMay(true);
        declareAttackers(List.of(1));
        assertThat(bear.isTapped()).isFalse();

        declareAttackers(List.of(1));
        assertThat(bear.isTapped()).isTrue();
    }
}
