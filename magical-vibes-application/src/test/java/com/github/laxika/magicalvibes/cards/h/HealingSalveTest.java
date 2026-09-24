package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgothianSwine.class, Forest.class, GoblinRaider.class, GrizzlyBears.class, HealingSalve.class, ProdigalSorcerer.class, Shock.class})
class HealingSalveTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Target player gains 3 life")
    class GainLifeMode {

        @Test
        @DisplayName("Target player gains 3 life")
        void targetPlayerGainsLife() {
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            int before = gd.playerLifeTotals.get(player2.getId());

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(before + 3);
        }

        @Test
        @DisplayName("Cannot target a creature with the gain-life mode")
        void cannotTargetCreature() {
            Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSwine());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, creature.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Prevent the next 3 damage to any target")
    class PreventDamageMode {

        @Test
        @DisplayName("Adds a 3-damage prevention shield to a target creature")
        void shieldOnCreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, bears.getId());
            harness.passBothPriorities();

            assertThat(bears.getDamagePreventionShield()).isEqualTo(3);
        }

        @Test
        @DisplayName("Adds a 3-damage prevention shield to a target player")
        void shieldOnPlayer() {
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        }

        @Test
        @DisplayName("Prevents combat damage to the targeted creature")
        void preventsCombatDamageToTargetCreature() {
            Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
            Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, blocker.getId());
            harness.passBothPriorities();

            declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
            prepareDeclareBlockers(player2);
            int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
            int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
            gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
            resolveCombat(player2);

            assertThat(blocker.getDamagePreventionShield()).isEqualTo(1);
            assertThat(blocker.getMarkedDamage()).isZero();
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        }

        @Test
        @DisplayName("Prevents only the next 3 damage to the targeted player")
        void preventsOnlyNextThreeDamageToTargetPlayer() {
            harness.setLife(player2, 20);
            Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
            Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            declareAttackers(player1, List.of(
                    gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                    gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
            resolveCombat(player1);

            harness.assertLife(player2, 19);
            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
        }

        @Test
        @DisplayName("Prevents noncombat damage to the targeted creature")
        void preventsNoncombatDamageToTargetCreature() {
            Permanent bears = addCreatureReady(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, bears.getId());
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castAndResolveInstant(player1, 0, bears.getId());

            assertThat(bears.getMarkedDamage()).isZero();
            assertThat(bears.getDamagePreventionShield()).isEqualTo(1);
        }

        @Test
        @DisplayName("Prevention shield expires at the end of the turn")
        void preventionShieldExpiresAtEndOfTurn() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);
            harness.castAndResolveInstant(player2, 0, player2.getId());

            harness.assertLife(player2, 18);
        }
    }

    @Nested
    @DisplayName("Mode 1: Prevent the next 3 damage to any target")
    class AdditionalPreventDamageCoverage {

        @Test
        @DisplayName("Adds a 3-damage prevention shield to a target creature")
        void shieldOnCreature() {
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArgothianSwine());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, creature.getId());
            harness.passBothPriorities();

            assertThat(creature.getDamagePreventionShield()).isEqualTo(3);
        }

        @Test
        @DisplayName("Requires a target for the prevention mode")
        void requiresTarget() {
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, null))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot target a land with the prevention mode")
        void cannotTargetLand() {
            Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, forest.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Adds a 3-damage prevention shield to a target player")
        void shieldOnPlayer() {
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        }

        @Test
        @DisplayName("Prevents combat damage to the targeted creature")
        void preventsCombatDamageToTargetCreature() {
            Permanent blocker = addCreatureReady(player1, new ArgothianSwine());
            Permanent attacker = addCreatureReady(player2, new GoblinRaider());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, blocker.getId());
            harness.passBothPriorities();

            declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
            prepareDeclareBlockers(player2);
            int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
            int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
            gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
            resolveCombat(player2);

            assertThat(blocker.getDamagePreventionShield()).isEqualTo(1);
            assertThat(blocker.getMarkedDamage()).isZero();
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        }

        @Test
        @DisplayName("Prevents only the next 3 damage to the targeted player")
        void preventsOnlyNextThreeDamageToTargetPlayer() {
            harness.setLife(player2, 20);
            Permanent firstAttacker = addCreatureReady(player1, new GoblinRaider());
            Permanent secondAttacker = addCreatureReady(player1, new GoblinRaider());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            declareAttackers(player1, List.of(
                    gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                    gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
            resolveCombat(player1);

            harness.assertLife(player2, 19);
            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
        }

        @Test
        @DisplayName("Prevents noncombat damage to the targeted player")
        void preventsNoncombatDamageToTargetPlayer() {
            harness.setLife(player2, 20);
            Permanent damageSource = addCreatureReady(player1, new ProdigalSorcerer());
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            harness.activateAbility(player1,
                    gd.playerBattlefields.get(player1.getId()).indexOf(damageSource),
                    null, player2.getId());
            harness.passBothPriorities();

            harness.assertLife(player2, 20);
            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        }

        @Test
        @DisplayName("The prevention shield expires at the end of the turn")
        void shieldExpiresAtEndOfTurn() {
            harness.setHand(player1, List.of(new HealingSalve()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castInstant(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(3);

            harness.forceStep(TurnStep.END_STEP);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
        }
    }
}
