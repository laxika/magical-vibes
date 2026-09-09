package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeedleshotGourna.class, EkunduGriffin.class})
class NeedleshotGournaTest extends BaseCardTest {

    @Test
    @DisplayName("Reach allows Needleshot Gourna to block a flying creature")
    void reachAllowsBlockingFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new EkunduGriffin());
        Permanent blocker = addCreatureReady(player2, new NeedleshotGourna());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.getBlockingTargetIds()).contains(attacker.getId());
    }
}
