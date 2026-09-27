package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ClergyOfTheHolyNimbus;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.cards.k.KeepersOfTheFaith;
import com.github.laxika.magicalvibes.cards.t.TheWretched;
import com.github.laxika.magicalvibes.cards.w.WallOfEarth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfiniteAuthority.class, ClergyOfTheHolyNimbus.class, DurkwoodBoars.class,
        KeepersOfTheFaith.class, Karakas.class, TheWretched.class, WallOfEarth.class})
class InfiniteAuthorityTest extends BaseCardTest {

    @Test
    @DisplayName("Infinite Authority can be cast targeting a creature and attaches on resolution")
    void attachesToCreatureOnResolution() {
        Permanent creature = addCreatureReady(player1, new KeepersOfTheFaith());
        harness.setHand(player1, List.of(new InfiniteAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof InfiniteAuthority
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Infinite Authority cannot target a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Karakas());
        harness.setHand(player1, List.of(new InfiniteAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("When the enchanted creature becomes blocked by a creature with toughness 3 or less, that creature is destroyed and the enchanted creature gets a counter")
    void becomesBlockedByLowToughnessCreature() {
        Permanent attacker = addCreatureReady(player1, new KeepersOfTheFaith());
        addAuthorityAttachedTo(player1, attacker);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new KeepersOfTheFaith());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When the enchanted creature blocks a creature with toughness 3 or less, that creature is destroyed at end of combat")
    void blocksLowToughnessCreature() {
        Permanent blocker = addCreatureReady(player1, new KeepersOfTheFaith());
        addAuthorityAttachedTo(player1, blocker);
        Permanent attacker = addCreatureReady(player2, new KeepersOfTheFaith());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature with toughness greater than 3 is not destroyed")
    void highToughnessCreatureSurvives() {
        Permanent attacker = addCreatureReady(player1, new DurkwoodBoars());
        addAuthorityAttachedTo(player1, attacker);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WallOfEarth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("When multiple creatures with toughness 3 or less block the enchanted creature, each is destroyed and grants a counter")
    void becomesBlockedByMultipleLowToughnessCreatures() {
        Permanent attacker = addCreatureReady(player1, new TheWretched());
        addAuthorityAttachedTo(player1, attacker);
        attacker.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new KeepersOfTheFaith());
        Permanent secondBlocker = addCreatureReady(player2, new KeepersOfTheFaith());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        resolveAllTriggers();
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(firstBlocker.getId(), 1, secondBlocker.getId(), 1));
        resolveAllTriggers();

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(firstBlocker, secondBlocker);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature that regenerates from the destruction remains on the battlefield and does not grant a counter")
    void regenerationPreventsDestructionReward() {
        Permanent attacker = addCreatureReady(player1, new DurkwoodBoars());
        addAuthorityAttachedTo(player1, attacker);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ClergyOfTheHolyNimbus());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addAuthorityAttachedTo(Player player, Permanent creature) {
        Permanent aura = new Permanent(new InfiniteAuthority());
        gd.playerBattlefields.get(player.getId()).add(aura);
        aura.setAttachedTo(creature.getId());
    }
}
