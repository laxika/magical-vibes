package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DawnsLightArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast during the opponent's turn thanks to Flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new DawnsLightArcher()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        gs.passPriority(gd, player2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dawn's Light Archer");
    }

    @Test
    @DisplayName("Reach lets Dawn's Light Archer block a creature with flying")
    void reachCanBlockFlyer() {
        Permanent flyer = addReadyAttacker(player1, new SuntailHawk());
        Permanent archer = addReadyBlocker(player2, new DawnsLightArcher());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, archer), indexOf(player1, flyer))));

        assertThat(archer.isBlocking()).isTrue();
    }

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBlocker(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
