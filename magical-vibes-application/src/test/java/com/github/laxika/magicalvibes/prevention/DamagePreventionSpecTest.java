package com.github.laxika.magicalvibes.prevention;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Family-level regression spec for the damage-prevention machinery, pinned BEFORE collapsing the
 * shield-creating {@code Prevent*Effect} records onto a parameterized spec record. These tests fix
 * the cross-cutting <em>consumption</em> semantics of the shield state slots
 * ({@code DamagePreventionService}) that no single card test pins: consumption ordering across
 * shield kinds, non-consumption when a stronger prevention already applies, combat/noncombat
 * windows, one-decrement-per-event color prevention, and the damage-can't-be-prevented bypass.
 * Per-card creation semantics stay pinned by the ~54 user-card tests.
 */
class DamagePreventionSpecTest extends BaseCardTest {

    @Nested
    @DisplayName("Shield consumption ordering")
    class ConsumptionOrdering {

        @Test
        @DisplayName("Global shield is consumed before a creature's own shield")
        void globalConsumedBeforeCreatureShield() {
            Permanent bears = addCreature(player2);
            gd.globalDamagePreventionShield = 1;
            bears.setDamagePreventionShield(5);

            shockPermanent(bears.getId());

            assertThat(gd.globalDamagePreventionShield).isZero();
            assertThat(bears.getDamagePreventionShield()).isEqualTo(4);
            assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        }

        @Test
        @DisplayName("A creature shield persists across damage events until exhausted")
        void creatureShieldPersistsAcrossEvents() {
            Permanent bears = addCreature(player2);
            bears.setDamagePreventionShield(3);

            harness.setHand(player1, List.of(new Shock(), new Shock()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, bears.getId());
            harness.passBothPriorities();
            assertThat(bears.getDamagePreventionShield()).isEqualTo(1);

            harness.castInstant(player1, 0, bears.getId());
            harness.passBothPriorities();

            // Second Shock: 1 prevented, 1 marked — the 2/2 survives with 1 damage.
            assertThat(bears.getDamagePreventionShield()).isZero();
            assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        }

        @Test
        @DisplayName("Full player prevention does not consume the player's amount shield")
        void fullPlayerPreventionDoesNotConsumeAmountShield() {
            gd.playersWithAllDamagePrevented.add(player2.getId());
            gd.playerDamagePreventionShields.put(player2.getId(), 3);
            harness.setLife(player2, 20);

            shockPlayer2();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Combat / noncombat windows")
    class CombatWindows {

        @Test
        @DisplayName("A combat-only creature flag does not stop noncombat damage")
        void combatOnlyCreatureFlagDoesNotStopNoncombat() {
            Permanent bears = addCreature(player2);
            gd.creaturesWithCombatDamagePrevented.add(bears.getId());

            shockPermanent(bears.getId());

            // Shock's 2 noncombat damage kills the 2/2 despite the Foxfire-style combat flag.
            assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        }

        @Test
        @DisplayName("Fog's combat-only flag does not stop noncombat damage to a player")
        void fogFlagDoesNotStopNoncombatPlayerDamage() {
            gd.preventAllCombatDamage = true;
            harness.setLife(player2, 20);

            shockPlayer2();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }

        @Test
        @DisplayName("Prevent-all-to-creatures does not protect players")
        void allCreaturesFlagDoesNotProtectPlayers() {
            gd.preventAllDamageToAllCreatures = true;
            harness.setLife(player2, 20);

            shockPlayer2();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }

        @Test
        @DisplayName("Damage-from-attackers player flag prevents unblocked combat damage")
        void attackersFlagPreventsCombatDamage() {
            gd.playersWithDamageFromAttackersPrevented.add(player2.getId());
            harness.setLife(player2, 20);

            Permanent attacker = new Permanent(new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(attacker);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Color prevention")
    class ColorPrevention {

        @Test
        @DisplayName("A color-prevention charge absorbs one whole damage event and is decremented once")
        void colorChargeConsumedOncePerEvent() {
            harness.setLife(player2, 20);
            Map<CardColor, Integer> charges = new HashMap<>();
            charges.put(CardColor.RED, 1);
            gd.playerColorDamagePreventionCount.put(player2.getId(), charges);

            harness.setHand(player1, List.of(new Shock(), new Shock()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
            // First red event fully prevented, charge consumed.
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            assertThat(gd.playerColorDamagePreventionCount.get(player2.getId()).get(CardColor.RED)).isZero();

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
            // No charge left: the second Shock lands in full.
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }
    }

    @Nested
    @DisplayName("Damage can't be prevented")
    class CantBePrevented {

        @Test
        @DisplayName("The turn flag bypasses shields without consuming them")
        void bypassesShieldsWithoutConsuming() {
            gd.damageCantBePreventedThisTurn = true;
            gd.globalDamagePreventionShield = 3;
            harness.setLife(player2, 20);

            shockPlayer2();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
            assertThat(gd.globalDamagePreventionShield).isEqualTo(3);
        }
    }

    // ===== Helpers =====

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(creature);
        return creature;
    }

    private void shockPermanent(UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void shockPlayer2() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
