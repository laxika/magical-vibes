package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addReadyDancer() {
        Permanent dancer = new Permanent(new MercurialSpelldancer());
        dancer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(dancer);
        return dancer;
    }

    private void dealCombatDamage() {
        Permanent dancer = gd.playerBattlefields.get(player1.getId()).getFirst();
        dancer.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
    }
}
