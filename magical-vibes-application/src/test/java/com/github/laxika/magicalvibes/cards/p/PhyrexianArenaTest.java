package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PhyrexianArena.class)
class PhyrexianArenaTest extends BaseCardTest {

    @Test
    @DisplayName("Controller draws a card and loses 1 life at upkeep")
    void drawsAndLosesLifeAtUpkeep() {
        harness.addToBattlefield(player1, new PhyrexianArena());
        harness.setLibrary(player1, List.of(new PhyrexianArena()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
        harness.assertInHand(player1, "Phyrexian Arena");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Each copy triggers independently during its controller's upkeep")
    void eachCopyTriggersIndependently() {
        harness.addToBattlefield(player1, new PhyrexianArena());
        harness.addToBattlefield(player1, new PhyrexianArena());
        harness.setLibrary(player1, List.of(new PhyrexianArena(), new PhyrexianArena()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new PhyrexianArena());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
