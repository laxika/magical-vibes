package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CrenellatedWall;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RampartCrawler.class, CrenellatedWall.class, FreshVolunteers.class})
class RampartCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Rampart Crawler can't be blocked by a Wall")
    void cannotBeBlockedByWall() {
        Permanent blocker = addCreatureReady(player2, new CrenellatedWall());
        Permanent attacker = addCreatureReady(player1, new RampartCrawler());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rampart Crawler can be blocked by a non-Wall creature")
    void canBeBlockedByNonWall() {
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        addCreatureReady(player1, new RampartCrawler()).setAttacking(true);
        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
