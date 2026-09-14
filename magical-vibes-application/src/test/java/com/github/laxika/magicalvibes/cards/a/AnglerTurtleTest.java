package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnglerTurtle.class, GrizzlyBears.class, Shock.class})
class AnglerTurtleTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents' creatures must attack each combat if able")
    void opponentCreaturesMustAttack() {
        harness.addToBattlefield(player1, new AnglerTurtle());
        addReady(player2, new GrizzlyBears());

        beginDeclareAttackers(player2);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("The controller's creatures are not forced to attack")
    void controllerCreaturesAreNotForced() {
        harness.addToBattlefield(player1, new AnglerTurtle());
        Permanent bears = addReady(player1, new GrizzlyBears());

        beginDeclareAttackers(player1);

        gs.declareAttackers(gd, player1, List.of());

        org.assertj.core.api.Assertions.assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Hexproof prevents an opponent from targeting Angler Turtle")
    void hexproofPreventsOpponentTargeting() {
        Permanent turtle = addReady(player1, new AnglerTurtle());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, turtle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
