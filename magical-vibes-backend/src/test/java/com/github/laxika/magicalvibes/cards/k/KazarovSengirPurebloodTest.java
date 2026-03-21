package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KazarovSengirPurebloodTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Kazarov has ON_OPPONENT_CREATURE_DEALT_DAMAGE trigger with PutCounterOnSelfEffect")
    void hasCorrectTriggeredEffect() {
        KazarovSengirPureblood card = new KazarovSengirPureblood();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DEALT_DAMAGE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DEALT_DAMAGE).getFirst())
                .isInstanceOf(PutCounterOnSelfEffect.class);
        PutCounterOnSelfEffect counterEffect = (PutCounterOnSelfEffect) card.getEffects(EffectSlot.ON_OPPONENT_CREATURE_DEALT_DAMAGE).getFirst();
        assertThat(counterEffect.counterType()).isEqualTo(CounterType.PLUS_ONE_PLUS_ONE);
    }

    @Test
    @DisplayName("Kazarov has activated ability: {3}{R}: deal 2 damage to target creature")
    void hasCorrectActivatedAbility() {
        KazarovSengirPureblood card = new KazarovSengirPureblood();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{3}{R}");
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(DealDamageToTargetCreatureEffect.class);
        DealDamageToTargetCreatureEffect damageEffect = (DealDamageToTargetCreatureEffect) ability.getEffects().getFirst();
        assertThat(damageEffect.damage()).isEqualTo(2);
    }

    // ===== Triggered ability: non-combat damage =====

    @Nested
    @DisplayName("Triggered ability — non-combat damage")
    class NonCombatDamageTrigger {

        @Test
        @DisplayName("When a spell deals damage to an opponent's creature, Kazarov gets a +1/+1 counter")
        void spellDamageToOpponentCreatureAddsCounter() {
            harness.addToBattlefield(player1, new KazarovSengirPureblood());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castInstant(player1, 0, bearsId);
            harness.passBothPriorities(); // Resolve Shock — 2 damage to Grizzly Bears

            // Kazarov trigger should be on the stack
            assertThat(gd.stack).hasSize(1);

            // Resolve the trigger
            harness.passBothPriorities();

            // Kazarov should have 1 +1/+1 counter
            Permanent kazarov = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Kazarov, Sengir Pureblood"))
                    .findFirst().orElseThrow();
            assertThat(kazarov.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        @DisplayName("Kazarov's own activated ability dealing damage to opponent's creature also triggers the counter")
        void ownAbilityDamageTriggersCounter() {
            harness.addToBattlefield(player1, new KazarovSengirPureblood());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addMana(player1, ManaColor.COLORLESS, 3);
            harness.addMana(player1, ManaColor.RED, 1);

            int kazarovIndex = 0;
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            harness.activateAbility(player1, kazarovIndex, null, bearsId);
            harness.passBothPriorities(); // Resolve ability — 2 damage to Grizzly Bears

            // Kazarov trigger should be on the stack
            assertThat(gd.stack).hasSize(1);

            // Resolve the trigger
            harness.passBothPriorities();

            // Kazarov should have 1 +1/+1 counter
            Permanent kazarov = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Kazarov, Sengir Pureblood"))
                    .findFirst().orElseThrow();
            assertThat(kazarov.getPlusOnePlusOneCounters()).isEqualTo(1);
        }

        @Test
        @DisplayName("Damage to controller's own creature does NOT trigger Kazarov")
        void damageToOwnCreatureDoesNotTrigger() {
            harness.addToBattlefield(player1, new KazarovSengirPureblood());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);

            // Player2 shocks player1's Grizzly Bears
            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
            harness.castInstant(player2, 0, bearsId);
            harness.passBothPriorities(); // Resolve Shock

            // Kazarov's trigger should NOT fire (damage was dealt to controller's creature, not opponent's)
            assertThat(gd.stack).isEmpty();

            Permanent kazarov = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Kazarov, Sengir Pureblood"))
                    .findFirst().orElseThrow();
            assertThat(kazarov.getPlusOnePlusOneCounters()).isEqualTo(0);
        }
    }

    // ===== Triggered ability: combat damage =====

    @Nested
    @DisplayName("Triggered ability — combat damage")
    class CombatDamageTrigger {

        @Test
        @DisplayName("When combat damage is dealt to opponent's creature, Kazarov gets a +1/+1 counter")
        void combatDamageToOpponentCreatureAddsCounter() {
            harness.addToBattlefield(player1, new KazarovSengirPureblood());
            harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2 attacker
            harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2 blocker

            Permanent attacker = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElseThrow();
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);

            Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
            blocker.setSummoningSick(false);
            blocker.setBlocking(true);
            blocker.addBlockingTarget(1); // Blocking the Grizzly Bears (index 1 on player1's bf)

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();

            // Resolve combat damage
            harness.passBothPriorities();

            // Kazarov trigger should be on the stack (opponent's creature was dealt damage)
            assertThat(gd.stack).isNotEmpty();

            // Resolve all triggers
            while (!gd.stack.isEmpty()) {
                harness.passBothPriorities();
            }

            // Kazarov should have a +1/+1 counter
            Permanent kazarov = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Kazarov, Sengir Pureblood"))
                    .findFirst().orElseThrow();
            assertThat(kazarov.getPlusOnePlusOneCounters()).isGreaterThanOrEqualTo(1);
        }
    }
}
