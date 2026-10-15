package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloriousCharge.class, RuneclawBear.class})
class GloriousChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new GloriousCharge(), "{1}{W}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Resolving boosts all own creatures +1/+1")
    void resolvingBoostsAllOwnCreatures() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.castFromHand(player1, new GloriousCharge(), "{1}{W}");
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(1);
                assertThat(p.getToughnessModifier()).isEqualTo(1);
                assertThat(p.getEffectivePower()).isEqualTo(3);
                assertThat(p.getEffectiveToughness()).isEqualTo(3);
            }
        }
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.castFromHand(player1, new GloriousCharge(), "{1}{W}");
        harness.passBothPriorities();

        // Player1's creature is boosted
        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : p1Battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(1);
                assertThat(p.getToughnessModifier()).isEqualTo(1);
            }
        }

        // Player2's creature is NOT boosted
        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        for (Permanent p : p2Battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(0);
                assertThat(p.getToughnessModifier()).isEqualTo(0);
            }
        }
    }

    @Test
    @DisplayName("Boost resets at cleanup step")
    void boostResetsAtCleanup() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.castFromHand(player1, new GloriousCharge(), "{1}{W}");
        harness.passBothPriorities();

        harness.passUntil(TurnStep.CLEANUP);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(0);
                assertThat(p.getToughnessModifier()).isEqualTo(0);
                assertThat(p.getEffectivePower()).isEqualTo(2);
                assertThat(p.getEffectiveToughness()).isEqualTo(2);
            }
        }
    }

    @Test
    @DisplayName("Works with empty battlefield (no crash)")
    void worksWithEmptyBattlefield() {
        harness.castFromHand(player1, new GloriousCharge(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Glorious Charge goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.castFromHand(player1, new GloriousCharge(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Glorious Charge");
    }

    @Test
    @DisplayName("Only creatures present when Glorious Charge resolves receive its boost")
    void snapshotsCreaturesAtResolution() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.castFromHand(player1, new GloriousCharge(), "{1}{W}");
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());

        assertThat(existing.getEffectivePower()).isEqualTo(3);
        assertThat(existing.getEffectiveToughness()).isEqualTo(3);
        assertThat(beforeResolution.getEffectivePower()).isEqualTo(3);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(3);
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Glorious Charges stack and all expire at cleanup")
    void multipleChargesStackUntilCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.castFromHand(player1, new GloriousCharge(), "{1}{W}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new GloriousCharge(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }
}
