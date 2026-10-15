package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GladewalkerRitualist.class, GrizzlyBears.class})
class GladewalkerRitualistTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when another Gladewalker Ritualist enters under your control")
    void drawsForAnotherControlledCopyEntering() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new GladewalkerRitualist());

        harness.enterBattlefieldAndReturn(player1, new GladewalkerRitualist());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Does not trigger when the first Gladewalker Ritualist enters")
    void doesNotTriggerForItsOwnEntry() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.enterBattlefieldAndReturn(player1, new GladewalkerRitualist());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Each existing copy triggers for a new matching creature")
    void eachExistingCopyTriggers() {
        GrizzlyBears firstDraw = new GrizzlyBears();
        GrizzlyBears secondDraw = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addToBattlefield(player1, new GladewalkerRitualist());
        harness.addToBattlefield(player1, new GladewalkerRitualist());

        harness.enterBattlefieldAndReturn(player1, new GladewalkerRitualist());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("Ignores other creatures and copies entering under an opponent's control")
    void ignoresNonmatchingEntries() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addToBattlefield(player1, new GladewalkerRitualist());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new GladewalkerRitualist());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }
}
