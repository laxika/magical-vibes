package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudcrownOak.class, AvianChangeling.class, WoodlandChangeling.class})
class CloudcrownOakTest extends BaseCardTest {

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new AvianChangeling());
        Permanent oak = addCreatureReady(player2, new CloudcrownOak());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(oak.isBlocking()).isTrue();
    }

    @Test
    void reachAllowsBlockingNonFlyingCreature() {
        addCreatureReady(player1, new WoodlandChangeling());
        Permanent oak = addCreatureReady(player2, new CloudcrownOak());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(oak.isBlocking()).isTrue();
    }

    @Test
    void reachDoesNotPreventNonFlyingCreatureFromBlockingOak() {
        addCreatureReady(player1, new CloudcrownOak());
        Permanent changeling = addCreatureReady(player2, new WoodlandChangeling());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(changeling.isBlocking()).isTrue();
    }
}
