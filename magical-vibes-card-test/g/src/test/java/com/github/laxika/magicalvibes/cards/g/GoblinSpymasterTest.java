package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinSpymaster.class, GrizzlyBears.class})
class GoblinSpymasterTest extends BaseCardTest {

    @Test
    void createsTokenOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new GoblinSpymaster());

        advanceToEndStep(player2);

        assertThat(tokenCount(player1)).isZero();
        assertThat(tokenCount(player2)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerOnControllersEndStep() {
        harness.addToBattlefield(player1, new GoblinSpymaster());

        advanceToEndStep(player1);

        assertThat(tokenCount(player1)).isZero();
    }

    @Test
    void createdTokenMakesAllCreaturesItsControllerControlsAttackIncludingItself() {
        harness.addToBattlefield(player1, new GoblinSpymaster());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        advanceToEndStep(player2);

        Permanent token = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        token.setSummoningSick(false);

        beginDeclareAttackers(player2);

        int bearsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(bearsIndex)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void createdTokenDoesNotForceOpponentCreaturesToAttack() {
        harness.addToBattlefield(player1, new GoblinSpymaster());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToEndStep(player2);
        beginDeclareAttackers(player1);

        gs.declareAttackers(gd, player1, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    private long tokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
