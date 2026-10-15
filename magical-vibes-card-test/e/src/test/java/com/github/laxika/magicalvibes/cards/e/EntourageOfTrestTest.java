package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EntourageOfTrest.class, GrizzlyBears.class})
class EntourageOfTrestTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller becomes the monarch when it enters")
    void becomesMonarchOnEntry() {
        harness.enterBattlefieldAndReturn(player1, new EntourageOfTrest());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Can block an additional creature while its controller is the monarch")
    void canBlockAdditionalCreatureWhileMonarch() {
        Permanent entourage = addCreatureReady(player2, new EntourageOfTrest());
        gd.monarchPlayerId = player2.getId();

        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0, 1));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));

        assertThat(entourage.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Cannot block an additional creature while its controller is not the monarch")
    void cannotBlockAdditionalCreatureWithoutMonarch() {
        addCreatureReady(player2, new EntourageOfTrest());
        gd.monarchPlayerId = player1.getId();

        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Entry trigger takes the monarchy even after its source leaves")
    void entryTriggerResolvesWithoutSource() {
        gd.monarchPlayerId = player2.getId();
        Permanent entourage = harness.enterBattlefieldAndReturn(player1, new EntourageOfTrest());
        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(entourage);
        gd.playerGraveyards.get(player1.getId()).add(entourage.getCard());

        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Monarch's Entourage cannot block three creatures")
    void cannotBlockThreeCreaturesWhileMonarch() {
        addCreatureReady(player2, new EntourageOfTrest());
        gd.monarchPlayerId = player2.getId();
        addCreatureReady(player1, new EntourageOfTrest());
        addCreatureReady(player1, new EntourageOfTrest());
        addCreatureReady(player1, new EntourageOfTrest());
        declareAttackersAndPrepareBlockers(List.of(0, 1, 2));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Cannot block an additional creature when there is no monarch")
    void cannotBlockAdditionalCreatureWhenNoMonarch() {
        addCreatureReady(player2, new EntourageOfTrest());
        gd.monarchPlayerId = null;
        addCreatureReady(player1, new EntourageOfTrest());
        addCreatureReady(player1, new EntourageOfTrest());
        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }
}
