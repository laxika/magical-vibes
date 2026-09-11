package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OldGnawbone.class, GrizzlyBears.class})
class OldGnawboneTest extends BaseCardTest {

    @Test
    @DisplayName("Creates that many Treasures when a creature you control deals combat damage")
    void createsTreasureTokensEqualToCombatDamage() {
        harness.addToBattlefield(player1, new OldGnawbone());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Uses the actual combat damage dealt for the Treasure count")
    void scalesWithCombatDamage() {
        harness.addToBattlefield(player1, new OldGnawbone());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
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
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
}
