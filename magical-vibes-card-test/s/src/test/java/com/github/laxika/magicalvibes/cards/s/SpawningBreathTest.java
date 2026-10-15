package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GideonJura;
import com.github.laxika.magicalvibes.cards.z.ZulaportEnforcer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpawningBreath.class, Forest.class, GideonJura.class, ZulaportEnforcer.class})
class SpawningBreathTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a player and creates an Eldrazi Spawn")
    void dealsDamageAndCreatesSpawn() {
        harness.setHand(player1, List.of(new SpawningBreath()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
    }

    @Test
    @DisplayName("An Eldrazi Spawn created by Spawning Breath can be sacrificed for colorless mana")
    void spawnSacrificeAddsColorlessMana() {
        castAndResolve();

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
        harness.activateAbility(player1, spawnIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("Spawning Breath cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SpawningBreath()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lethal damage to a creature still creates a Spawn for the caster")
    void killsCreatureAndCreatesSpawn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ZulaportEnforcer());
        harness.setHand(player1, List.of(new SpawningBreath()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Zulaport Enforcer");
        harness.assertInGraveyard(player2, "Zulaport Enforcer");
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
        assertThat(findPermanents(player2, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("Deals damage directly to a planeswalker and creates a Spawn")
    void damagesPlaneswalkerAndCreatesSpawn() {
        Permanent gideon = harness.addToBattlefieldAndReturn(player2, new GideonJura());
        gideon.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new SpawningBreath()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, gideon.getId());

        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
    }

    @Test
    @DisplayName("Prevented damage does not prevent Spawn creation")
    void createsSpawnWhenDamageIsPrevented() {
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonJura());
        gideon.setCounterCount(CounterType.LOYALTY, 6);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new SpawningBreath()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, gideon.getId());

        harness.assertOnBattlefield(player1, "Gideon Jura");
        assertThat(gideon.getMarkedDamage()).isZero();
        assertThat(gideon.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
    }

    @Test
    @DisplayName("No Spawn is created when the sole target is sacrificed before resolution")
    void doesNotCreateSpawnWhenTargetBecomesIllegal() {
        castAndResolve();
        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        harness.setHand(player1, List.of(new SpawningBreath()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, spawn.getId());

        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
        harness.activateAbility(player1, spawnIndex, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        harness.assertInGraveyard(player1, "Spawning Breath");
        harness.assertLife(player2, 19);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new SpawningBreath()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }
}
