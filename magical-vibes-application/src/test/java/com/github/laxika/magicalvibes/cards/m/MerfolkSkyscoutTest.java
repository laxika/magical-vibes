package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MerfolkSkyscoutTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking untaps target permanent")
    void attackingUntapsTargetPermanent() {
        addReadySkyscout(player1);
        Permanent island = addPermanent(player2, new Island());
        island.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, island.getId());
        harness.passBothPriorities();

        assertThat(island.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Blocking untaps target permanent")
    void blockingUntapsTargetPermanent() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent island = addPermanent(player1, new Island());
        island.tap();
        addReadySkyscout(player2);

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.handlePermanentChosen(player2, island.getId());
        harness.passBothPriorities();

        assertThat(island.isTapped()).isFalse();
    }

    private Permanent addReadySkyscout(Player player) {
        Permanent perm = new Permanent(new MerfolkSkyscout());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
