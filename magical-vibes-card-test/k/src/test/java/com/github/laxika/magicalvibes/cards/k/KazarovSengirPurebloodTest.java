package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.ArcTrail;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingGrace;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KazarovSengirPureblood.class, ArcTrail.class, GrizzlyBears.class,
        InvasionOfInnistrad.class, LilianaVess.class, Shock.class, HealingGrace.class})
class KazarovSengirPurebloodTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped, summoning-sick Kazarov can activate repeatedly and target itself or an ally")
    void activatedAbilityDoesNotRequireTapAndCanTargetOwnCreatures() {
        Permanent kazarov = harness.addToBattlefieldAndReturn(player1, new KazarovSengirPureblood());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        kazarov.tap();
        kazarov.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, kazarov.getId());
        harness.passBothPriorities();

        assertThat(kazarov.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(kazarov.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(kazarov.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Damage to two opposing creatures gives one counter per creature, not per damage point")
    void damageToTwoCreaturesAddsTwoCounters() {
        Permanent kazarov = harness.addToBattlefieldAndReturn(player1, new KazarovSengirPureblood());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArcTrail()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(gd.stack).hasSize(2);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(kazarov.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage to an opposing player does not trigger Kazarov")
    void damageToOpponentPlayerDoesNotTrigger() {
        Permanent kazarov = harness.addToBattlefieldAndReturn(player1, new KazarovSengirPureblood());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(kazarov.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Fully prevented damage does not trigger Kazarov")
    void preventedDamageDoesNotTrigger() {
        Permanent kazarov = harness.addToBattlefieldAndReturn(player1, new KazarovSengirPureblood());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new HealingGrace()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handlePermanentChosen(player2, kazarov.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(kazarov.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Nested
    @DisplayName("Triggered ability — non-combat damage")
    @CardUsed({KazarovSengirPureblood.class, GrizzlyBears.class, Shock.class})
    class NonCombatDamageTrigger {

        @Test
        @DisplayName("When a spell deals damage to an opponent's creature, Kazarov gets a +1/+1 counter")
        void spellDamageToOpponentCreatureAddsCounter() {
            harness.addToBattlefield(player1, new KazarovSengirPureblood());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castAndResolveInstant(player1, 0, bearsId);

            // Kazarov trigger should be on the stack
            assertThat(gd.stack).hasSize(1);

            // Resolve the trigger
            harness.passBothPriorities();

            // Kazarov should have 1 +1/+1 counter
            Permanent kazarov = findPermanent(player1, "Kazarov, Sengir Pureblood");
            assertThat(kazarov.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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
            Permanent kazarov = findPermanent(player1, "Kazarov, Sengir Pureblood");
            assertThat(kazarov.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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
            harness.castAndResolveInstant(player2, 0, bearsId);

            // Kazarov's trigger should NOT fire (damage was dealt to controller's creature, not opponent's)
            assertThat(gd.stack).isEmpty();

            Permanent kazarov = findPermanent(player1, "Kazarov, Sengir Pureblood");
            assertThat(kazarov.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Triggered ability — combat damage")
    @CardUsed({KazarovSengirPureblood.class, GrizzlyBears.class})
    class CombatDamageTrigger {

        @Test
        @DisplayName("When combat damage is dealt to opponent's creature, Kazarov gets a +1/+1 counter")
        void combatDamageToOpponentCreatureAddsCounter() {
            harness.addToBattlefield(player1, new KazarovSengirPureblood());
            harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2 attacker
            harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2 blocker

            Permanent attacker = findPermanent(player1, "Grizzly Bears");
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
            Permanent kazarov = findPermanent(player1, "Kazarov, Sengir Pureblood");
            assertThat(kazarov.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isGreaterThanOrEqualTo(1);
        }
    }

    /**
     * "Whenever a creature an opponent controls is dealt damage" — a permanent an opponent controls
     * that is not a creature does not match that trigger event (CR 603.2), so an any-target spell
     * aimed at a planeswalker or a battle leaves Kazarov alone.
     */
    @Nested
    @DisplayName("Triggered ability — non-creature any-target permanents")
    @CardUsed({KazarovSengirPureblood.class, LilianaVess.class, ArcTrail.class,
            InvasionOfInnistrad.class, Shock.class})
    class NonCreatureAnyTargets {

        @Test
        @DisplayName("Damage to an opponent's planeswalker does not trigger Kazarov")
        void damageToOpponentPlaneswalkerDoesNotTrigger() {
            harness.addToBattlefield(player1, new KazarovSengirPureblood());
            Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
            liliana.setCounterCount(CounterType.LOYALTY, 5);
            harness.setHand(player1, List.of(new ArcTrail()));
            harness.addMana(player1, ManaColor.RED, 2);

            // 2 damage to the planeswalker, 1 to its controller — no creature is damaged at all.
            harness.castAndResolveSorcery(player1, 0, List.of(liliana.getId(), player2.getId()));

            assertThat(gd.stack).isEmpty();
            assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);

            Permanent kazarov = findPermanent(player1, "Kazarov, Sengir Pureblood");
            assertThat(kazarov.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        }

        @Test
        @DisplayName("Damage to an opponent's battle does not trigger Kazarov")
        void damageToOpponentBattleDoesNotTrigger() {
            harness.addToBattlefield(player1, new KazarovSengirPureblood());
            Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
            battle.setCounterCount(CounterType.DEFENSE, 5);
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castAndResolveInstant(player1, 0, battle.getId());

            assertThat(gd.stack).isEmpty();
            assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);

            Permanent kazarov = findPermanent(player1, "Kazarov, Sengir Pureblood");
            assertThat(kazarov.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        }
    }
}
