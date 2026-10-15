package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OldGnawbone.class, DireWolfProwler.class})
class OldGnawboneTest extends BaseCardTest {

    @Test
    @DisplayName("Creates that many Treasures when a creature you control deals combat damage")
    void createsTreasureTokensEqualToCombatDamage() {
        harness.addToBattlefield(player1, new OldGnawbone());
        Permanent attacker = addCreatureReady(player1, new DireWolfProwler());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Uses the actual combat damage dealt for the Treasure count")
    void scalesWithCombatDamage() {
        harness.addToBattlefield(player1, new OldGnawbone());
        Permanent attacker = addCreatureReady(player1, new DireWolfProwler());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
    }

    @Test
    @DisplayName("Does not trigger when a creature you control deals no combat damage to a player")
    void doesNotTriggerWhenBlocked() {
        harness.addToBattlefield(player1, new OldGnawbone());
        Permanent attacker = addCreatureReady(player1, new DireWolfProwler());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DireWolfProwler());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Old Gnawbone's own combat damage creates Treasures")
    void triggersForItsOwnCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new OldGnawbone());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(7);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Each creature dealing combat damage creates its own Treasures")
    void triggersForEachDamagingCreature() {
        harness.addToBattlefield(player1, new OldGnawbone());
        Permanent first = addCreatureReady(player1, new DireWolfProwler());
        Permanent second = addCreatureReady(player1, new DireWolfProwler());
        first.setAttacking(true);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setAttacking(true);

        resolveCombat();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(5);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Opponent's creatures do not trigger Old Gnawbone")
    void doesNotTriggerForOpponentsCombatDamage() {
        harness.addToBattlefield(player1, new OldGnawbone());
        Permanent attacker = addCreatureReady(player2, new DireWolfProwler());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Treasure creation still resolves after Old Gnawbone leaves the battlefield")
    void triggerResolvesWithoutOldGnawbone() {
        harness.addToBattlefield(player1, new OldGnawbone());
        Permanent attacker = addCreatureReady(player1, new DireWolfProwler());
        attacker.setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        Permanent source = findPermanent(player1, "Old Gnawbone");
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }
}
