package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViashinoBeyTest extends BaseCardTest {

    private void beginDeclareAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addReadyCreature(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("Viashino Bey forces all creatures its controller controls to attack")
    void ownCreaturesMustAttack() {
        harness.addToBattlefield(player1, new ViashinoBey());
        addReadyCreature(player1, new GrizzlyBears());

        beginDeclareAttackers(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Viashino Bey does not force an opponent's creatures to attack")
    void opponentsCreaturesAreNotForced() {
        harness.addToBattlefield(player1, new ViashinoBey());
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        beginDeclareAttackers(player2);

        gs.declareAttackers(gd, player2, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }
}
