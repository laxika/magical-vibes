package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Viseling.class, Mossdog.class, IvoryMask.class})
class ViselingTest extends BaseCardTest {

    private List<Card> mossdogs(int count) {
        return Stream.generate(Mossdog::new).limit(count).map(Card.class::cast).toList();
    }

    @Test
    @DisplayName("Deals damage equal to the opponent's hand size minus four")
    void dealsScalingDamage() {
        harness.addToBattlefield(player1, new Viseling());
        harness.setHand(player2, mossdogs(6));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals no damage when the opponent has four or fewer cards in hand")
    void noDamageWithFourOrFewerCards() {
        harness.addToBattlefield(player1, new Viseling());
        harness.setHand(player2, mossdogs(4));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger during the controller's own upkeep")
    void doesNotTriggerDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new Viseling());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Recomputes damage from the opponent's hand size at resolution")
    void amountRecomputedAtResolution() {
        harness.addToBattlefield(player1, new Viseling());
        harness.setHand(player2, mossdogs(6));

        advanceToUpkeep(player2);
        harness.setHand(player2, mossdogs(8));
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @CardUsed(IvoryMask.class)
    @DisplayName("Player shroud does not stop the non-targeting upkeep trigger")
    void shroudDoesNotStopNonTargetingTrigger() {
        harness.addToBattlefield(player1, new Viseling());
        harness.addToBattlefield(player2, new IvoryMask());
        harness.setHand(player2, mossdogs(6));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An empty opponent hand does not cause damage or life gain")
    void emptyHandDealsNoDamage() {
        harness.addToBattlefield(player1, new Viseling());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The ability triggers below the threshold and uses cards gained before resolution")
    void handGrowsAcrossThresholdBeforeResolution() {
        harness.addToBattlefield(player1, new Viseling());
        harness.setHand(player2, mossdogs(3));

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, mossdogs(7));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("No damage is dealt when the hand drops below the threshold before resolution")
    void handShrinksAcrossThresholdBeforeResolution() {
        harness.addToBattlefield(player1, new Viseling());
        harness.setHand(player2, mossdogs(7));

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, mossdogs(2));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No ability triggers on the controller's upkeep even with a large hand")
    void ownUpkeepDoesNotTriggerWithLargeHands() {
        harness.addToBattlefield(player1, new Viseling());
        harness.setHand(player1, mossdogs(7));
        harness.setHand(player2, mossdogs(7));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
