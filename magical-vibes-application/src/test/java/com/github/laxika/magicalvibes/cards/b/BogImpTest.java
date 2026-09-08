package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BogImp.class, GrizzlyBears.class, StormCrow.class, GiantSpider.class})
class BogImpTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Bog Imp")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent imp = addCreatureReady(player1, new BogImp());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(imp);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("A creature with flying can block Bog Imp")
    void flyingCreatureCanBlock() {
        Permanent imp = addCreatureReady(player1, new BogImp());
        Permanent blocker = addCreatureReady(player2, new StormCrow());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(imp);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with reach can block Bog Imp")
    void reachCreatureCanBlock() {
        Permanent imp = addCreatureReady(player1, new BogImp());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(imp);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
