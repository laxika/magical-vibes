package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControllerLifeAtOrBelowThresholdConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClingingMistsTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Clinging Mists has PreventAllCombatDamageEffect and two fateful hour conditional effects")
    void hasCorrectEffects() {
        ClingingMists card = new ClingingMists();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(PreventAllCombatDamageEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ControllerLifeAtOrBelowThresholdConditionalEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(2)).isInstanceOf(ControllerLifeAtOrBelowThresholdConditionalEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ClingingMists()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Clinging Mists");
    }

    // ===== Base effect: prevent all combat damage =====

    @Nested
    @DisplayName("Combat damage prevention")
    class CombatDamagePrevention {

        @Test
        @DisplayName("Prevents all combat damage after resolving")
        void preventsAllCombatDamage() {
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.preventAllCombatDamage).isTrue();
        }

        @Test
        @DisplayName("Goes to graveyard after resolving")
        void goesToGraveyardAfterResolving() {
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Clinging Mists"));
        }
    }

    // ===== Fateful hour: tap + skip untap on attacking creatures =====

    @Nested
    @DisplayName("Fateful hour")
    class FatefulHour {

        @Test
        @DisplayName("Taps attacking creatures when controller has 5 life")
        void tapsAttackingCreaturesAtFiveLife() {
            Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);

            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(attacker.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Sets skipUntapCount on attacking creatures when controller has 5 life")
        void setsSkipUntapOnAttackingCreaturesAtFiveLife() {
            Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);

            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Taps attacking creatures when controller has less than 5 life")
        void tapsAttackingCreaturesBelowFiveLife() {
            Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);

            harness.setLife(player1, 1);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(attacker.isTapped()).isTrue();
            assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Does not tap or freeze attacking creatures when controller has more than 5 life")
        void doesNotTapOrFreezeAboveFiveLife() {
            Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);

            harness.setLife(player1, 6);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            // Attacker may be tapped from attacking, but not from the spell
            assertThat(attacker.getSkipUntapCount()).isZero();
        }

        @Test
        @DisplayName("Does not tap or freeze attacking creatures at default 20 life")
        void doesNotTapOrFreezeAtDefaultLife() {
            Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);

            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(attacker.getSkipUntapCount()).isZero();
        }

        @Test
        @DisplayName("Does not affect non-attacking creatures even with fateful hour")
        void doesNotAffectNonAttackingCreatures() {
            Permanent nonAttacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            nonAttacker.setSummoningSick(false);

            Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);

            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(nonAttacker.isTapped()).isFalse();
            assertThat(nonAttacker.getSkipUntapCount()).isZero();
            assertThat(attacker.isTapped()).isTrue();
            assertThat(attacker.getSkipUntapCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Affected creatures do not untap during next untap step")
        void affectedCreaturesDoNotUntapDuringNextUntapStep() {
            Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);

            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(attacker.isTapped()).isTrue();
            assertThat(attacker.getSkipUntapCount()).isEqualTo(1);

            // Advance to player2's untap step
            advanceToNextTurn(player1);

            // Creature should still be tapped (skip untap consumed)
            assertThat(attacker.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Affected creatures untap normally on the turn after")
        void affectedCreaturesUntapOnFollowingTurn() {
            Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);

            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            // Advance through player2's turn (skip untap consumed)
            advanceToNextTurn(player1);
            assertThat(attacker.isTapped()).isTrue();

            // Advance through player1's turn
            advanceToNextTurn(player2);

            // Advance to player2's next turn — creature should untap now
            advanceToNextTurn(player1);
            assertThat(attacker.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Still prevents combat damage even with fateful hour active")
        void stillPreventsCombatDamageWithFatefulHour() {
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.setLife(player1, 5);
            harness.setHand(player1, List.of(new ClingingMists()));
            harness.addMana(player1, ManaColor.GREEN, 3);

            harness.castInstant(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.preventAllCombatDamage).isTrue();
        }
    }

    // ===== Helpers =====

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }
}
