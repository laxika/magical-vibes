package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.d.DissipationField;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DamageTriggerCollectorServiceTest extends BaseCardTest {

    @Nested
    @DisplayName("ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU — ReturnDamageSourcePermanentToHandEffect")
    class BounceOnDamage {

        @Test
        @DisplayName("Dissipation Field bounces the damage source to its owner's hand")
        void dissipationFieldBouncesSource() {
            harness.addToBattlefield(player1, new DissipationField());
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            UUID bearsId = bears.getId();

            harness.getTriggerCollectionService().checkDamageDealtToControllerTriggers(
                    gd, player1.getId(), bearsId, true);

            // Bears should no longer be on the battlefield
            boolean bearsOnBattlefield = gd.playerBattlefields.get(player2.getId()).stream()
                    .anyMatch(p -> p.getId().equals(bearsId));
            assertThat(bearsOnBattlefield).isFalse();

            // Bears should be in owner's hand
            harness.assertInHand(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Dissipation Field stops processing further triggers after bouncing")
        void dissipationFieldStopsProcessingAfterBounce() {
            harness.addToBattlefield(player1, new DissipationField());
            harness.addToBattlefield(player1, new DissipationField());
            harness.addToBattlefield(player2, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
            UUID bearsId = bears.getId();

            harness.getTriggerCollectionService().checkDamageDealtToControllerTriggers(
                    gd, player1.getId(), bearsId, true);

            // Should still work fine — first Dissipation Field bounces, second has no source to bounce
            harness.assertInHand(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Does not trigger when source permanent is null")
        void doesNotTriggerWithNullSource() {
            harness.addToBattlefield(player1, new DissipationField());
            int battlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

            harness.getTriggerCollectionService().checkDamageDealtToControllerTriggers(
                    gd, player1.getId(), null, true);

            // Nothing should have changed
            assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore);
        }
    }

    @Nested
    @DisplayName("ON_DEALT_DAMAGE — default handler")
    class DealtDamageToCreature {

        @Test
        @DisplayName("Does nothing when creature has no ON_DEALT_DAMAGE effects")
        void noEffectsNoTrigger() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();

            int stackBefore = gd.stack.size();
            harness.getTriggerCollectionService().checkDealtDamageToCreatureTriggers(
                    gd, bears, 2, player2.getId());

            assertThat(gd.stack).hasSize(stackBefore);
        }
    }
}
