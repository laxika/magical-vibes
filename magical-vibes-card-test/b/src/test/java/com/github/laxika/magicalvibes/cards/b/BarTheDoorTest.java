package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.h.HeavyMattock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({BarTheDoor.class, DawntreaderElk.class, HeavyMattock.class})
class BarTheDoorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Resolving boosts all own creatures +0/+4")
    void resolvingBoostsAllOwnCreatures() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(PermanentGrants.powerBoostOf(gd, p)).isEqualTo(0);
                assertThat(PermanentGrants.toughnessBoostOf(gd, p)).isEqualTo(4);
                assertThat(gqs.getEffectivePower(gd, p)).isEqualTo(2);
                assertThat(gqs.getEffectiveToughness(gd, p)).isEqualTo(6);
            }
        }
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addToBattlefield(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        // Player1's creature is boosted
        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : p1Battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(PermanentGrants.powerBoostOf(gd, p)).isEqualTo(0);
                assertThat(PermanentGrants.toughnessBoostOf(gd, p)).isEqualTo(4);
            }
        }

        // Player2's creature is NOT boosted
        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        for (Permanent p : p2Battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(PermanentGrants.powerBoostOf(gd, p)).isEqualTo(0);
                assertThat(PermanentGrants.toughnessBoostOf(gd, p)).isEqualTo(0);
            }
        }
    }

    @Test
    @DisplayName("Boost resets at cleanup step")
    void boostResetsAtCleanup() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(PermanentGrants.powerBoostOf(gd, p)).isEqualTo(0);
                assertThat(PermanentGrants.toughnessBoostOf(gd, p)).isEqualTo(0);
                assertThat(gqs.getEffectivePower(gd, p)).isEqualTo(2);
                assertThat(gqs.getEffectiveToughness(gd, p)).isEqualTo(2);
            }
        }
    }

    @Test
    @DisplayName("Works with empty battlefield (no crash)")
    void worksWithEmptyBattlefield() {
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bar the Door goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Bar the Door");
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void doesNotBoostCreaturesEnteringLater() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new DawntreaderElk());

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering before resolution receive the boost")
    void boostsCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0);
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new DawntreaderElk());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Multiple resolutions stack and both boosts expire at cleanup")
    void multipleBoostsStackUntilCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new BarTheDoor(), new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncreature permanents do not receive the boost")
    void doesNotBoostNoncreaturePermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HeavyMattock());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(PermanentGrants.powerBoostOf(gd, artifact)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, artifact)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }
}
