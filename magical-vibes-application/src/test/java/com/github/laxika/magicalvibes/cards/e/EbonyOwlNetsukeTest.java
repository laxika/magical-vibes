package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AkkiUnderling;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EbonyOwlNetsuke.class, AkkiUnderling.class})
class EbonyOwlNetsukeTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's upkeep with seven cards in hand deals 4 damage to that opponent")
    void opponentUpkeepWithSevenCardsDealsDamage() {
        harness.addToBattlefield(player1, new EbonyOwlNetsuke());
        harness.setHand(player2, List.of(new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(),
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Opponent's upkeep with six cards in hand does nothing")
    void opponentUpkeepWithSixCardsDoesNothing() {
        harness.addToBattlefield(player1, new EbonyOwlNetsuke());
        harness.setHand(player2, List.of(new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(),
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Controller's own upkeep does not trigger")
    void ownUpkeepDoesNothing() {
        harness.addToBattlefield(player1, new EbonyOwlNetsuke());
        harness.setHand(player1, List.of(new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(),
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Intervening-if is rechecked at resolution")
    void interveningIfCheckedAtResolution() {
        harness.addToBattlefield(player1, new EbonyOwlNetsuke());
        harness.setHand(player2, List.of(new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(),
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerHands.get(player2.getId()).remove(0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Reaching seven cards after upkeep begins does not create the trigger")
    void thresholdMustBeMetWhenUpkeepBegins() {
        harness.addToBattlefield(player1, new EbonyOwlNetsuke());
        harness.setHand(player2, List.of(new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling(),
                new AkkiUnderling(), new AkkiUnderling(), new AkkiUnderling()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerHands.get(player2.getId()).add(new AkkiUnderling());

        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }
}
