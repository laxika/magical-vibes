package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void castTicketTortoise() {
        harness.setHand(player1, List.of(new TicketTortoise()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
