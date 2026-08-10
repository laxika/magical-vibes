package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MindstormCrownTest extends BaseCardTest {

    @Test
    @DisplayName("Draws when the controller had no cards at the beginning of the turn")
    void drawsWhenHandWasEmptyAtTurnStart() {
        harness.addToBattlefield(player1, new MindstormCrown());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.handSizeAtTurnStart.put(player1.getId(), 0);
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Deals 1 damage when the controller had a card at the beginning of the turn")
    void dealsDamageWhenHandWasNotEmptyAtTurnStart() {
        harness.addToBattlefield(player1, new MindstormCrown());
        harness.setHand(player1, List.of());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Uses the start-of-turn hand size rather than the current hand")
    void usesStartOfTurnSnapshot() {
        harness.addToBattlefield(player1, new MindstormCrown());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.handSizeAtTurnStart.put(player1.getId(), 0);
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
