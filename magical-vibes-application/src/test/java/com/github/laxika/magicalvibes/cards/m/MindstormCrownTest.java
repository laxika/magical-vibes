package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindstormCrown.class, YotianSoldier.class})
class MindstormCrownTest extends BaseCardTest {

    @Test
    @DisplayName("Draws when the controller had no cards at the beginning of the turn")
    void drawsWhenHandWasEmptyAtTurnStart() {
        harness.addToBattlefield(player1, new MindstormCrown());
        harness.setHand(player1, List.of(new YotianSoldier()));
        harness.setLibrary(player1, List.of(new YotianSoldier()));
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
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new MindstormCrown());
        harness.setHand(player1, List.of());
        gd.handSizeAtTurnStart.put(player1.getId(), 1);
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);
    }

    @Test
    @DisplayName("Uses the start-of-turn hand size rather than the current hand")
    void usesStartOfTurnSnapshot() {
        harness.addToBattlefield(player1, new MindstormCrown());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new YotianSoldier()));
        gd.handSizeAtTurnStart.put(player1.getId(), 0);
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        gd.playerHands.get(player1.getId()).add(new YotianSoldier());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
