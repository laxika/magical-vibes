package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoreProwler.class, GrizzlyBears.class, WrathOfGod.class})
class CoreProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked infect combat damage gives poison without reducing life")
    void unblockedCombatGivesPoison() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new CoreProwler());
        prowler.setSummoningSick(false);
        prowler.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Core Prowler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Death trigger proliferates every kind of counter on a selected player only")
    void proliferatePlayerAddsEveryExistingKind() {
        harness.addToBattlefield(player1, new CoreProwler());
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        gd.setPlayerEnergyCounters(player2.getId(), 3);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.assertInGraveyard(player1, "Core Prowler");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    /**
     * Sets up combat where Core Prowler (player1, 2/2 infect) attacks and is blocked by a 3/3 creature (player2).
     * Core Prowler will die from combat damage, and the blocker will get 2 -1/-1 counters from infect.
     */
    private void setupCombatWhereCoreProwlerDies() {
        Permanent prowlerPerm = findPermanent(player1, "Core Prowler");
        prowlerPerm.setSummoningSick(false);
        prowlerPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Casting Core Prowler puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new CoreProwler()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Core Prowler");
    }

    @Test
    @DisplayName("When Core Prowler dies in combat, death trigger puts proliferate on the stack")
    void deathTriggerPutsProliferateOnStack() {
        harness.addToBattlefield(player1, new CoreProwler());
        setupCombatWhereCoreProwlerDies();

        harness.passBothPriorities(); // Combat damage — Core Prowler dies

        // Core Prowler should be dead
        harness.assertInGraveyard(player1, "Core Prowler");

        // Death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Core Prowler");
    }

    @Test
    @DisplayName("Proliferate from death trigger adds -1/-1 counter to chosen creature")
    void deathTriggerProliferateAddsMinusCounters() {
        harness.addToBattlefield(player1, new CoreProwler());

        // Add a creature with an existing -1/-1 counter
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        setupCombatWhereCoreProwlerDies();
        harness.passBothPriorities(); // Combat damage — Core Prowler dies

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Choose the bears with -1/-1 counter for proliferate
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate from death trigger adds counters to blocker that received infect -1/-1 counters")
    void deathTriggerProliferateAddsCountersToBlocker() {
        harness.addToBattlefield(player1, new CoreProwler());
        setupCombatWhereCoreProwlerDies();

        // Get reference to the blocker (3/3 bear that will receive 2 -1/-1 counters from infect)
        Permanent blocker = findPermanent(player2, "Grizzly Bears");

        harness.passBothPriorities(); // Combat damage — Core Prowler dies, blocker gets 2 -1/-1 counters

        // Blocker should have 2 -1/-1 counters from infect combat damage
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Choose the blocker for proliferate
        harness.handleMultiplePermanentsChosen(player1, List.of(blocker.getId()));

        // Blocker should now have 3 -1/-1 counters (2 from infect + 1 from proliferate)
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Proliferate from death trigger adds +1/+1 counter to chosen creature")
    void deathTriggerProliferateAddsPlusCounters() {
        harness.addToBattlefield(player1, new CoreProwler());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        setupCombatWhereCoreProwlerDies();
        harness.passBothPriorities(); // Core Prowler dies
        harness.passBothPriorities(); // Resolve triggered ability

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate can choose no permanents")
    void proliferateCanChooseNone() {
        harness.addToBattlefield(player1, new CoreProwler());

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        setupCombatWhereCoreProwlerDies();
        harness.passBothPriorities(); // Core Prowler dies
        harness.passBothPriorities(); // Resolve triggered ability

        harness.handleMultiplePermanentsChosen(player1, List.of());

        // Counter unchanged
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate can add counters to multiple permanents")
    void proliferateMultiplePermanents() {
        harness.addToBattlefield(player1, new CoreProwler());

        Permanent bears1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears1.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Permanent bears2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears2.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        setupCombatWhereCoreProwlerDies();
        harness.passBothPriorities(); // Core Prowler dies
        harness.passBothPriorities(); // Resolve triggered ability

        harness.handleMultiplePermanentsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        assertThat(bears1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears2.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate does nothing when no permanents have counters")
    void proliferateNoEligiblePermanents() {
        harness.addToBattlefield(player1, new CoreProwler());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Kill Core Prowler via Wrath of God (no infect combat damage, no counters on anything)
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Core Prowler should be dead
        harness.assertInGraveyard(player1, "Core Prowler");

        // Death trigger on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the triggered ability — no eligible permanents, no choice needed
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Death trigger from Wrath of God still triggers proliferate")
    void deathTriggerFromWrathStillTriggers() {
        harness.addToBattlefield(player1, new CoreProwler());

        // The other creature dies too, so its counters cannot be proliferated.
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Core Prowler death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve proliferate triggered ability
        harness.passBothPriorities();

        // All creatures died from Wrath, including the bears with counters
        // No eligible permanents should remain, no choice needed
        assertThat(gd.stack).isEmpty();
    }
}
