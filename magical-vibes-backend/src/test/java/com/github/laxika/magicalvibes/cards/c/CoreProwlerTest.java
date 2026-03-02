package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoreProwlerTest extends BaseCardTest {

    /**
     * Sets up combat where Core Prowler (player1, 2/2 infect) attacks and is blocked by a 3/3 creature (player2).
     * Core Prowler will die from combat damage, and the blocker will get 2 -1/-1 counters from infect.
     */
    private void setupCombatWhereCoreProwlerDies() {
        Permanent prowlerPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Core Prowler"))
                .findFirst().orElseThrow();
        prowlerPerm.setSummoningSick(false);
        prowlerPerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    // ===== Card structure =====

    @Test
    @DisplayName("Core Prowler has ON_DEATH ProliferateEffect")
    void hasCorrectEffects() {
        CoreProwler card = new CoreProwler();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).singleElement()
                .isInstanceOf(ProliferateEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Core Prowler puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new CoreProwler()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Core Prowler"));
    }

    // ===== Death trigger: proliferate after combat =====

    @Test
    @DisplayName("When Core Prowler dies in combat, death trigger puts proliferate on the stack")
    void deathTriggerPutsProliferateOnStack() {
        harness.addToBattlefield(player1, new CoreProwler());
        setupCombatWhereCoreProwlerDies();

        harness.passBothPriorities(); // Combat damage — Core Prowler dies

        // Core Prowler should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Core Prowler"));

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
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        setupCombatWhereCoreProwlerDies();
        harness.passBothPriorities(); // Combat damage — Core Prowler dies

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Choose the bears with -1/-1 counter for proliferate
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate from death trigger adds counters to blocker that received infect -1/-1 counters")
    void deathTriggerProliferateAddsCountersToBlocker() {
        harness.addToBattlefield(player1, new CoreProwler());
        setupCombatWhereCoreProwlerDies();

        // Get reference to the blocker (3/3 bear that will receive 2 -1/-1 counters from infect)
        Permanent blocker = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        harness.passBothPriorities(); // Combat damage — Core Prowler dies, blocker gets 2 -1/-1 counters

        // Blocker should have 2 -1/-1 counters from infect combat damage
        assertThat(blocker.getMinusOneMinusOneCounters()).isEqualTo(2);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Choose the blocker for proliferate
        harness.handleMultiplePermanentsChosen(player1, List.of(blocker.getId()));

        // Blocker should now have 3 -1/-1 counters (2 from infect + 1 from proliferate)
        assertThat(blocker.getMinusOneMinusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("Proliferate from death trigger adds +1/+1 counter to chosen creature")
    void deathTriggerProliferateAddsPlusCounters() {
        harness.addToBattlefield(player1, new CoreProwler());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setPlusOnePlusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        setupCombatWhereCoreProwlerDies();
        harness.passBothPriorities(); // Core Prowler dies
        harness.passBothPriorities(); // Resolve triggered ability

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    // ===== Proliferate choices =====

    @Test
    @DisplayName("Proliferate can choose no permanents")
    void proliferateCanChooseNone() {
        harness.addToBattlefield(player1, new CoreProwler());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        setupCombatWhereCoreProwlerDies();
        harness.passBothPriorities(); // Core Prowler dies
        harness.passBothPriorities(); // Resolve triggered ability

        harness.handleMultiplePermanentsChosen(player1, List.of());

        // Counter unchanged
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferate can add counters to multiple permanents")
    void proliferateMultiplePermanents() {
        harness.addToBattlefield(player1, new CoreProwler());

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setPlusOnePlusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears2);

        setupCombatWhereCoreProwlerDies();
        harness.passBothPriorities(); // Core Prowler dies
        harness.passBothPriorities(); // Resolve triggered ability

        harness.handleMultiplePermanentsChosen(player1, List.of(bears1.getId(), bears2.getId()));

        assertThat(bears1.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(bears2.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    // ===== No eligible permanents =====

    @Test
    @DisplayName("Proliferate does nothing when no permanents have counters")
    void proliferateNoEligiblePermanents() {
        harness.addToBattlefield(player1, new CoreProwler());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Kill Core Prowler via Wrath of God (no infect combat damage, no counters on anything)
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath — all creatures die

        // Core Prowler should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Core Prowler"));

        // Death trigger on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the triggered ability — no eligible permanents, no choice needed
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Death trigger via Wrath of God =====

    @Test
    @DisplayName("Death trigger from Wrath of God still triggers proliferate")
    void deathTriggerFromWrathStillTriggers() {
        harness.addToBattlefield(player1, new CoreProwler());

        // Add a creature with counters that survives (not a creature, so Wrath won't kill it)
        // Use a Grizzly Bears with +1/+1 counter that is NOT on the battlefield (won't be killed)
        // Actually, let's add a non-creature permanent or a creature that already has counters
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setPlusOnePlusOneCounters(2);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath — all creatures die

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
