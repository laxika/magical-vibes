package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkkiBattleSquad.class, GrizzlyBears.class})
class AkkiBattleSquadTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with a modified creature untaps modified creatures and creates an additional combat")
    void modifiedAttackUntapsModifiedCreaturesAndCreatesAdditionalCombat() {
        addCreatureReady(player1, new AkkiBattleSquad());
        Permanent modifiedAttacker = addCreatureReady(player1, new GrizzlyBears());
        modifiedAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent modifiedHome = addCreatureReady(player1, new GrizzlyBears());
        modifiedHome.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent unmodifiedHome = addCreatureReady(player1, new GrizzlyBears());
        modifiedHome.tap();
        unmodifiedHome.tap();

        declareAkkiAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(modifiedAttacker.isTapped()).isFalse();
        assertThat(modifiedHome.isTapped()).isFalse();
        assertThat(unmodifiedHome.isTapped()).isTrue();
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking only with unmodified creatures does not trigger")
    void unmodifiedAttackDoesNotTrigger() {
        addCreatureReady(player1, new AkkiBattleSquad());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAkkiAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(1);
        assertThat(attacker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The trigger does not happen again in the same turn")
    void triggersOnlyOnceEachTurn() {
        addCreatureReady(player1, new AkkiBattleSquad());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAkkiAttackers(List.of(1));
        harness.passBothPriorities();

        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(0);
    }

    private void declareAkkiAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gd.combatPhasesThisTurn = 1;
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackerIndices);
    }
}
