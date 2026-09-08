package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnagTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast by discarding a Forest")
    void canBeCastByDiscardingForest() {
        harness.setHand(player1, List.of(new Snag(), new Forest()));

        castWithForestDiscard(0, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Snag");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Alternate cost requires a Forest card")
    void alternateCostRequiresForest() {
        harness.setHand(player1, List.of(new Snag(), new GrizzlyBears()));

        assertThatThrownBy(() -> castWithForestDiscard(0, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents combat damage from unblocked creatures but not blocked creatures")
    void preventsDamageFromUnblockedCreatures() {
        harness.setLife(player2, 20);
        Permanent unblockedAttacker = addReadyCreature(player1);
        Permanent blockedAttacker = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);

        declareAttackers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(unblockedAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(blockedAttacker)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(blockedAttacker))));

        harness.setHand(player1, List.of(new Snag(), new Forest()));
        castWithForestDiscard(0, 1);
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void castWithForestDiscard(int cardIndex, Integer discardHandCardIndex) {
        gs.playCard(gd, player1, cardIndex, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, discardHandCardIndex);
    }
}
