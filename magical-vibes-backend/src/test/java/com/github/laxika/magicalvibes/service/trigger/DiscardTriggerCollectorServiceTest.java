package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Megrim;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscardTriggerCollectorServiceTest extends BaseCardTest {

    @Nested
    @DisplayName("ON_OPPONENT_DISCARDS — DealDamageToDiscardingPlayerEffect")
    class DiscardDamage {

        @Test
        @DisplayName("Megrim deals 2 damage when opponent discards a card")
        void megrimDealsDamageOnOpponentDiscard() {
            harness.addToBattlefield(player1, new Megrim());
            int lifeBefore = gd.playerLifeTotals.get(player2.getId());

            // Simulate opponent discarding a card
            Card discarded = new GrizzlyBears();
            harness.getTriggerCollectionService().checkDiscardTriggers(gd, player2.getId(), discarded);

            int lifeAfter = gd.playerLifeTotals.get(player2.getId());
            assertThat(lifeAfter).isEqualTo(lifeBefore - 2);
        }

        @Test
        @DisplayName("Megrim does not trigger when controller discards")
        void megrimDoesNotTriggerOnControllerDiscard() {
            harness.addToBattlefield(player1, new Megrim());
            int lifeBefore = gd.playerLifeTotals.get(player1.getId());

            Card discarded = new GrizzlyBears();
            harness.getTriggerCollectionService().checkDiscardTriggers(gd, player1.getId(), discarded);

            int lifeAfter = gd.playerLifeTotals.get(player1.getId());
            assertThat(lifeAfter).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("Multiple Megrims each deal damage independently")
        void multipleMegrimsEachDealDamage() {
            harness.addToBattlefield(player1, new Megrim());
            harness.addToBattlefield(player1, new Megrim());
            int lifeBefore = gd.playerLifeTotals.get(player2.getId());

            Card discarded = new GrizzlyBears();
            harness.getTriggerCollectionService().checkDiscardTriggers(gd, player2.getId(), discarded);

            int lifeAfter = gd.playerLifeTotals.get(player2.getId());
            assertThat(lifeAfter).isEqualTo(lifeBefore - 4);
        }
    }
}
