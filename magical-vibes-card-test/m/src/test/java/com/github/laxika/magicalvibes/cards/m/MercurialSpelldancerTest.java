package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ExpandTheSphere;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MercurialSpelldancer.class, GrizzlyBears.class, LightningBolt.class, Shock.class, ExpandTheSphere.class})
class MercurialSpelldancerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts an oil counter on Mercurial Spelldancer")
    void noncreatureSpellPutsOilCounter() {
        Permanent dancer = addReadyDancer();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(dancer.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not put an oil counter on Mercurial Spelldancer")
    void creatureSpellDoesNotPutOilCounter() {
        Permanent dancer = addReadyDancer();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(dancer.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Accepting the combat-damage trigger removes two oil counters and copies the next instant")
    void acceptingCombatDamageTriggerCopiesNextInstant() {
        Permanent dancer = addReadyDancer();
        dancer.setCounterCount(CounterType.OIL, 2);
        dealCombatDamage();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(dancer.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Copy Lightning Bolt"));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Declining the combat-damage trigger leaves oil counters and does not register a copy")
    void decliningCombatDamageTriggerDoesNothing() {
        Permanent dancer = addReadyDancer();
        dancer.setCounterCount(CounterType.OIL, 2);
        dealCombatDamage();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(dancer.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Having only one oil counter cannot pay the two-counter combat-damage cost")
    void oneOilCounterCannotPayCombatDamageCost() {
        Permanent dancer = addReadyDancer();
        dancer.setCounterCount(CounterType.OIL, 1);
        dealCombatDamage();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(dancer.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Mercurial Spelldancer cannot be blocked")
    void cannotBeBlocked() {
        Permanent dancer = addReadyDancer();
        harness.addToBattlefield(player2, new MercurialSpelldancer());
        dancer.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Paying for the combat trigger removes exactly two counters")
    void removesExactlyTwoOilCounters() {
        Permanent dancer = addReadyDancer();
        dancer.setCounterCount(CounterType.OIL, 5);
        dealCombatDamage();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(dancer.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("A departed Spelldancer cannot pay for the combat trigger")
    void departedSourceCannotCreateDelayedCopy() {
        Permanent dancer = addReadyDancer();
        dancer.setCounterCount(CounterType.OIL, 2);
        dealCombatDamage();
        gd.playerBattlefields.get(player1.getId()).remove(dancer);
        gd.playerGraveyards.get(player1.getId()).add(dancer.getCard());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("The copied spell resolves, does not add oil, and only the next spell is copied")
    void copyResolvesWithoutBeingCastAndOnlyOnce() {
        Permanent dancer = addReadyDancer();
        dancer.setCounterCount(CounterType.OIL, 2);
        dealCombatDamage();
        harness.handleMayAbilityChosen(player1, true);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 14);
        assertThat(dancer.getCounterCount(CounterType.OIL)).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 12);
        assertThat(dancer.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The spell copy can choose a different target")
    void copyCanChooseNewTarget() {
        Permanent dancer = addReadyDancer();
        dancer.setCounterCount(CounterType.OIL, 2);
        dealCombatDamage();
        harness.handleMayAbilityChosen(player1, true);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, first.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(dancer.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The delayed ability copies a sorcery after Spelldancer leaves")
    void copiesSorceryAfterSourceLeaves() {
        Permanent dancer = addReadyDancer();
        dancer.setCounterCount(CounterType.OIL, 2);
        dealCombatDamage();
        harness.handleMayAbilityChosen(player1, true);
        gd.playerBattlefields.get(player1.getId()).remove(dancer);
        gd.playerGraveyards.get(player1.getId()).add(dancer.getCard());
        harness.setHand(player1, List.of(new ExpandTheSphere()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castSorcery(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Copy Expand the Sphere"));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("The delayed copy ability expires at the end of the turn")
    void unusedCopyExpiresAtEndOfTurn() {
        Permanent dancer = addReadyDancer();
        dancer.setCounterCount(CounterType.OIL, 2);
        dealCombatDamage();
        harness.handleMayAbilityChosen(player1, true);
        harness.setLibrary(player1, List.of(new MercurialSpelldancer(), new MercurialSpelldancer()));
        harness.setLibrary(player2, List.of(new MercurialSpelldancer(), new MercurialSpelldancer()));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not add oil counters")
    void opponentsSpellDoesNotAddOil() {
        Permanent dancer = addReadyDancer();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(dancer.getCounterCount(CounterType.OIL)).isZero();
        harness.assertLife(player1, 18);
    }
    private Permanent addReadyDancer() {
        return addCreatureReady(player1, new MercurialSpelldancer());
    }

    private void dealCombatDamage() {
        Permanent dancer = gd.playerBattlefields.get(player1.getId()).getFirst();
        dancer.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
    }
}
