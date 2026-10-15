package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.w.WingCommando;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RocHunter.class, WingCommando.class})
class RocHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Reach lets Roc Hunter block a creature with flying")
    void reachCanBlockFlyer() {
        Permanent flyer = addCreatureReady(player1, new WingCommando());
        flyer.setAttacking(true);
        Permanent rocHunter = addCreatureReady(player2, new RocHunter());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(rocHunter),
                gd.playerBattlefields.get(player1.getId()).indexOf(flyer))));

        assertThat(rocHunter.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Reach does not restrict Roc Hunter to blocking flying creatures")
    void reachCreatureCanBlockAnotherReachCreature() {
        Permanent attacker = addCreatureReady(player1, new RocHunter());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RocHunter());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
