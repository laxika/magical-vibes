package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SylvanEchoesTest extends BaseCardTest {

    // ===== Won clash — may draw a card =====

    @Test
    @DisplayName("Won clash: accepting the may draws a card")
    void wonClashDrawsCard() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new SylvanEchoes());

        // Higher mana value on top for player1 (GrizzlyBears MV 2 > Forest MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.getTriggerCollectionService().performClash(gd, player1.getId());
        harness.passBothPriorities(); // resolve clash trigger → may-draw prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    // ===== Won clash — declining draws nothing =====

    @Test
    @DisplayName("Won clash: declining the may draws nothing")
    void wonClashDeclineDrawsNothing() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new SylvanEchoes());

        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.getTriggerCollectionService().performClash(gd, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).isEmpty();
    }

    // ===== Lost clash — no trigger at all =====

    @Test
    @DisplayName("Lost clash: no trigger, no draw prompt")
    void lostClashNoTrigger() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new SylvanEchoes());

        // Lower mana value on top for player1 (Forest MV 0 < GrizzlyBears MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.getTriggerCollectionService().performClash(gd, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).isEmpty();
    }
}
