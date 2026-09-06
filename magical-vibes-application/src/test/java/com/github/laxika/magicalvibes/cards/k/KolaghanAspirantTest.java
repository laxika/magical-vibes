package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KolaghanAspirant.class, GrizzlyBears.class})
class KolaghanAspirantTest extends BaseCardTest {

    @Test
    @DisplayName("When Kolaghan Aspirant becomes blocked, it deals 1 damage to the blocker")
    void becomingBlockedDamagesBlocker() {
        Permanent aspirant = addCreatureReady(player1, new KolaghanAspirant());
        aspirant.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Kolaghan Aspirant becomes blocked by two creatures, it deals 1 damage to each blocker")
    void becomingBlockedDamagesEachBlocker() {
        Permanent aspirant = addCreatureReady(player1, new KolaghanAspirant());
        aspirant.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(1);
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(1);
    }

}
