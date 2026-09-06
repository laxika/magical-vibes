package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MisersCage.class, BayFalcon.class})
class MisersCageTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's upkeep with five cards in hand deals 2 damage to that opponent")
    void opponentUpkeepWithFiveCardsDealsDamage() {
        harness.addToBattlefield(player1, new MisersCage());
        harness.setHand(player2, List.of(new BayFalcon(), new BayFalcon(), new BayFalcon(),
                new BayFalcon(), new BayFalcon()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Opponent's upkeep with four cards in hand does nothing")
    void opponentUpkeepWithFourCardsDoesNothing() {
        harness.addToBattlefield(player1, new MisersCage());
        harness.setHand(player2, List.of(new BayFalcon(), new BayFalcon(), new BayFalcon(),
                new BayFalcon()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Controller's own upkeep never triggers, even with a full hand")
    void ownUpkeepDoesNothing() {
        harness.addToBattlefield(player1, new MisersCage());
        harness.setHand(player1, List.of(new BayFalcon(), new BayFalcon(), new BayFalcon(),
                new BayFalcon(), new BayFalcon(), new BayFalcon()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Intervening-if is rechecked at resolution — no damage if the hand shrinks below five")
    void interveningIfCheckedAtResolution() {
        harness.addToBattlefield(player1, new MisersCage());
        harness.setHand(player2, List.of(new BayFalcon(), new BayFalcon(), new BayFalcon(),
                new BayFalcon(), new BayFalcon()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerHands.get(player2.getId()).remove(0); // down to four before the trigger resolves
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Reaching five cards after upkeep begins does not create the trigger")
    void thresholdMustBeMetWhenUpkeepBegins() {
        harness.addToBattlefield(player1, new MisersCage());
        harness.setHand(player2, List.of(new BayFalcon(), new BayFalcon(), new BayFalcon(),
                new BayFalcon()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.setHand(player2, List.of(new BayFalcon(), new BayFalcon(), new BayFalcon(),
                new BayFalcon(), new BayFalcon()));

        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }
}
