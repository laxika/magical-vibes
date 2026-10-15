package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiredCharge.class, RuneclawBear.class, Plains.class})
class InspiredChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new InspiredCharge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Resolving boosts all own creatures +2/+1")
    void resolvingBoostsAllOwnCreatures() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new InspiredCharge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(2);
                assertThat(p.getToughnessModifier()).isEqualTo(1);
                assertThat(p.getEffectivePower()).isEqualTo(4);
                assertThat(p.getEffectiveToughness()).isEqualTo(3);
            }
        }
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new InspiredCharge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        // Player1's creature is boosted
        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : p1Battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(2);
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
        harness.setHand(player1, List.of(new InspiredCharge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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
        harness.setHand(player1, List.of(new InspiredCharge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Inspired Charge goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new InspiredCharge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Inspired Charge");
    }

    @Test
    @DisplayName("Does not boost noncreature permanents")
    void doesNotBoostNoncreaturePermanents() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new InspiredCharge()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(land.getPowerModifier()).isZero();
        assertThat(land.getToughnessModifier()).isZero();
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Includes creatures that entered after casting but before resolution")
    void includesCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new InspiredCharge()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0);
        Permanent bear = harness.enterBattlefieldAndReturn(player1, new RuneclawBear());

        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void doesNotBoostCreaturesEnteringLater() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new InspiredCharge()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveInstant(player1, 0);

        Permanent later = harness.enterBattlefieldAndReturn(player1, new RuneclawBear());

        assertThat(original.getEffectivePower()).isEqualTo(4);
        assertThat(original.getEffectiveToughness()).isEqualTo(3);
        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Repeated casts stack their boosts until cleanup")
    void repeatedCastsStackUntilCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new InspiredCharge(), new InspiredCharge()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);

        assertThat(bear.getEffectivePower()).isEqualTo(6);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }
}
