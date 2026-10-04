package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraspingGiant.class, GrizzlyBears.class})
class GraspingGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles each blocker until Grasping Giant leaves the battlefield")
    void exilesEachBlockerUntilSourceLeaves() {
        Permanent giant = addCreatureReady(player1, new GraspingGiant());
        giant.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).filteredOn(entry -> entry.getCard().getName().equals("Grasping Giant"))
                .hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(2);

        giant.setMarkedDamage(7);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .isEmpty();
    }

    @Test
    @DisplayName("Does not exile the blocker if Grasping Giant leaves before the trigger resolves")
    void doesNotExileBlockerIfSourceLeavesBeforeResolution() {
        Permanent giant = addCreatureReady(player1, new GraspingGiant());
        giant.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        giant.setMarkedDamage(7);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
