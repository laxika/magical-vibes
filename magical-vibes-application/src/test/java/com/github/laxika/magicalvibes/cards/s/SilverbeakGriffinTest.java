package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SilverbeakGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Silverbeak Griffin can't be blocked by a creature without flying")
    void cannotBeBlockedByCreatureWithoutFlying() {
        Permanent blocker = addCreature(player2, new GrizzlyBears());
        Permanent attacker = addCreature(player1, new SilverbeakGriffin());
        attacker.setAttacking(true);

        prepareBlockers();

        assertThatThrownBy(() -> declareBlocker(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Silverbeak Griffin can be blocked by a creature with flying")
    void canBeBlockedByCreatureWithFlying() {
        Permanent blocker = addCreature(player2, new AirElemental());
        Permanent attacker = addCreature(player1, new SilverbeakGriffin());
        attacker.setAttacking(true);

        prepareBlockers();

        declareBlocker(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void prepareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private void declareBlocker(Permanent blocker, Permanent attacker) {
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
