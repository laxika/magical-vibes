package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BarTheDoorTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Bar the Door has correct card properties")
    void hasCorrectProperties() {
        BarTheDoor card = new BarTheDoor();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(BoostAllOwnCreaturesEffect.class);
        BoostAllOwnCreaturesEffect effect = (BoostAllOwnCreaturesEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(0);
        assertThat(effect.toughnessBoost()).isEqualTo(4);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Bar the Door");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving boosts all own creatures +0/+4")
    void resolvingBoostsAllOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(0);
                assertThat(p.getToughnessModifier()).isEqualTo(4);
                assertThat(p.getEffectivePower()).isEqualTo(2);
                assertThat(p.getEffectiveToughness()).isEqualTo(6);
            }
        }
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Player1's creature is boosted
        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : p1Battlefield) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getPowerModifier()).isEqualTo(0);
                assertThat(p.getToughnessModifier()).isEqualTo(4);
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
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

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
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bar the Door goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new BarTheDoor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bar the Door"));
    }
}
