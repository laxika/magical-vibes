package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArcTrail;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathPitsOfRath.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class DeathPitsOfRathTest extends BaseCardTest {

    @Test
    @DisplayName("A creature dealt non-lethal noncombat damage is destroyed")
    void noncombatDamageDestroysDamagedCreature() {
        harness.addToBattlefield(player1, new DeathPitsOfRath());
        harness.addToBattlefield(player2, new HillGiant()); // 3/3 survives 2 damage normally
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to the 3/3

        // Death Pits trigger should now be on the stack
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // Resolve the trigger

        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Death Pits destruction cannot be stopped by a regeneration shield")
    void destructionCannotBeRegenerated() {
        harness.addToBattlefield(player1, new DeathPitsOfRath());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        giant.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, giant.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Prevented damage does not trigger Death Pits")
    void preventedDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new DeathPitsOfRath());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        giant.setDamagePreventionShield(2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, giant.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(giant.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Death Pits destroys any creature — including its controller's own")
    void destroysControllersOwnCreature() {
        harness.addToBattlefield(player1, new DeathPitsOfRath());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities(); // Resolve Shock

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("A creature dealt non-lethal combat damage is destroyed by Death Pits")
    void combatDamageDestroysSurvivingBlocker() {
        harness.addToBattlefield(player1, new DeathPitsOfRath());
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2 attacker
        harness.addToBattlefield(player2, new HillGiant());    // 3/3 blocker, survives combat

        Permanent attacker = findPermanent(player1, "Grizzly Bears");
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = findPermanent(player2, "Hill Giant");
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1); // Grizzly Bears is index 1 on player1's battlefield

        // Resolve combat damage and the Death Pits trigger it queues.
        resolveCombat();
        resolveAllTriggers();

        // Hill Giant survived combat (3 toughness, took only 2) but Death Pits destroys it anyway.
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    /**
     * "Whenever a creature is dealt damage" — an any-target permanent that is not a creature is not
     * a creature (CR 603.2), so damaging a planeswalker or a battle must not queue the destroy.
     */
    @Nested
    @DisplayName("Non-creature any-target permanents")
    class NonCreatureAnyTargets {

        @Test
        @CardUsed({ArcTrail.class, LilianaVess.class})
        @DisplayName("Damaging a planeswalker does not trigger Death Pits, but the creature beside it still does")
        void planeswalkerDealtDamageDoesNotTrigger() {
            harness.addToBattlefield(player1, new DeathPitsOfRath());
            Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
            liliana.setCounterCount(CounterType.LOYALTY, 5);
            harness.addToBattlefield(player2, new HillGiant());
            harness.setHand(player1, List.of(new ArcTrail()));
            harness.addMana(player1, ManaColor.RED, 2);

            UUID giantId = harness.getPermanentId(player2, "Hill Giant");
            // 2 damage to the planeswalker, 1 to the creature.
            harness.castSorcery(player1, 0, List.of(liliana.getId(), giantId));
            harness.passBothPriorities(); // Resolve Arc Trail

            // Only the Hill Giant's trigger — the planeswalker did not queue one.
            assertThat(gd.stack).hasSize(1);

            harness.passBothPriorities(); // Resolve the trigger

            harness.assertNotOnBattlefield(player2, "Hill Giant");
            harness.assertOnBattlefield(player2, "Liliana Vess");
            assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        }

        @Test
        @CardUsed(InvasionOfInnistrad.class)
        @DisplayName("Damaging a battle does not trigger Death Pits")
        void battleDealtDamageDoesNotTrigger() {
            harness.addToBattlefield(player1, new DeathPitsOfRath());
            Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
            battle.setCounterCount(CounterType.DEFENSE, 5);
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, battle.getId());
            harness.passBothPriorities(); // Resolve Shock — 2 damage to the battle

            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player2, "Invasion of Innistrad");
            assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
        }
    }
}
