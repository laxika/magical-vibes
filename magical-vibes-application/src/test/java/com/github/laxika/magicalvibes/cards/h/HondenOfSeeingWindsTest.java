package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HondenOfSeeingWinds.class, HondenOfLifesWeb.class})
class HondenOfSeeingWindsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card for each Shrine its controller controls")
    void drawsForEachControlledShrine() {
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Counts Shrines when the upkeep trigger resolves")
    void recountsShrinesAtResolution() {
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Does not count Shrines controlled by an opponent")
    void ignoresOpponentControlledShrines() {
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.addToBattlefield(player1, new HondenOfLifesWeb());
        harness.addToBattlefield(player2, new HondenOfLifesWeb());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.addToBattlefield(player2, new HondenOfLifesWeb());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
