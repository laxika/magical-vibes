package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegionWarbossTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a hasty Goblin that must attack this combat")
    void createsGoblinThatMustAttackThisCombat() {
        Permanent warboss = addReadyWarboss(player1);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        beginDeclareAttackers(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(warboss))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        gs.declareAttackers(gd, player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(warboss),
                gd.playerBattlefields.get(player1.getId()).indexOf(token)));

        assertThat(token.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Mentor puts a +1/+1 counter on an attacking creature with lesser power")
    void mentorCountersLesserPowerAttacker() {
        Permanent warboss = addCreatureReady(player1, new LegionWarboss());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareWarbossAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(warboss),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a token on an opponent's turn")
    void noTokenOnOpponentsTurn() {
        harness.addToBattlefield(player1, new LegionWarboss());

        advanceToBeginningOfCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    private Permanent addReadyWarboss(Player player) {
        return addCreatureReady(player, new LegionWarboss());
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void declareWarbossAttackers(Player activePlayer, List<Integer> attackers) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, activePlayer, attackers);
    }
}
