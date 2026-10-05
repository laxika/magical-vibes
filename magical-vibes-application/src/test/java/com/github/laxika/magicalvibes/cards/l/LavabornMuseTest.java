package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LavabornMuse.class, GrizzlyBears.class})
class LavabornMuseTest extends BaseCardTest {

    // ===== Triggering =====

    @Test
    @DisplayName("Deals 3 damage when opponent has 2 cards in hand")
    void dealsDamageWithTwoCards() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Deals 3 damage when opponent has 1 card in hand")
    void dealsDamageWithOneCard() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Deals 3 damage when opponent has empty hand")
    void dealsDamageWithEmptyHand() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Does NOT trigger when opponent has 3 or more cards in hand")
    void doesNotTriggerWithThreeCards() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does NOT trigger if hand drops below threshold after upkeep begins")
    void doesNotTriggerWhenHandDropsAfterUpkeepBegins() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        gd.playerHands.get(player2.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does NOT trigger during controller's own upkeep")
    void doesNotTriggerDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player1, List.of()); // empty hand, condition would be met
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does NOT deal damage if opponent's hand grows above 2 before resolution")
    void interveningIfCheckedAtResolution() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player2, List.of(new GrizzlyBears())); // 1 card, triggers
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        // Trigger is on the stack — add cards to opponent's hand before resolution
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        // Now opponent has 3 cards in hand
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Deals damage when hand grows to exactly two cards before resolution")
    void dealsDamageAtResolutionThreshold() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 3);
    }

    @Test
    @DisplayName("Each Muse deals damage independently")
    void multipleMusesEachDealDamage() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertLife(player2, lifeBefore - 6);
    }

    @Test
    @DisplayName("An already triggered ability deals damage after its source leaves")
    void triggerResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player1, new LavabornMuse());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        var muse = gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.setGraveyard(player1, List.of(muse.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 3);
    }
}

