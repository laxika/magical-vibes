package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TicketTortoise.class, Forest.class})
class TicketTortoiseTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure when an opponent controls more lands")
    void createsTreasureWhenOpponentHasMoreLands() {
        castTicketTortoise();
        harness.addToBattlefield(player2, new Forest());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ticket Tortoise");
        harness.assertOnBattlefield(player1, "Treasure");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Treasure"))
                .singleElement().satisfies(p -> assertThat(p.isTapped()).isFalse());
        harness.assertNotOnBattlefield(player2, "Treasure");
    }

    @Test
    @DisplayName("Does not trigger when land counts are equal as it enters")
    void doesNotTriggerWhenLandCountsAreEqual() {
        castTicketTortoise();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    @Test
    @DisplayName("Does not create a Treasure if land counts equalize before the trigger resolves")
    void doesNotCreateTreasureWhenConditionFailsAtResolution() {
        castTicketTortoise();
        harness.addToBattlefield(player2, new Forest());

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    @Test
    @DisplayName("Does not trigger when its controller has more lands")
    void doesNotTriggerWhenControllerHasMoreLands() {
        harness.addToBattlefield(player1, new Forest());
        castTicketTortoise();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    @Test
    @DisplayName("A land advantage gained after entry does not create a trigger")
    void doesNotTriggerWhenOpponentGainsLandAfterEntry() {
        castTicketTortoise();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.addToBattlefield(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    @Test
    @DisplayName("Creates a Treasure if the condition becomes true again before resolution")
    void createsTreasureWhenConditionBecomesTrueAgain() {
        harness.addToBattlefield(player2, new Forest());
        castTicketTortoise();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Treasure");
        harness.assertNotOnBattlefield(player2, "Treasure");
    }

    @Test
    @DisplayName("The entering creature's controller gets the Treasure even without casting it")
    void createsTreasureForOpponentControllerWithoutCasting() {
        harness.addToBattlefield(player1, new Forest());
        harness.enterBattlefieldAndReturn(player2, new TicketTortoise());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Treasure");
        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    private void castTicketTortoise() {
        harness.castFromHand(player1, new TicketTortoise(), "{2}");
    }
}
