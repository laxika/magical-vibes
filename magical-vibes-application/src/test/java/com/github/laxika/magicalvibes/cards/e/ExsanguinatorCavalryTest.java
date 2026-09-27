package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExsanguinatorCavalry.class, YouthfulKnight.class, GrizzlyBears.class})
class ExsanguinatorCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a Knight and creates a Blood token when it deals combat damage")
    void knightCombatDamageAddsCounterAndBlood() {
        addCreatureReady(player1, new ExsanguinatorCavalry());
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());
        knight.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a non-Knight creature")
    void nonKnightCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new ExsanguinatorCavalry());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Blood")).isZero();
    }

    @Test
    @DisplayName("Does not trigger when a Knight is blocked")
    void blockedKnightCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new ExsanguinatorCavalry());
        Permanent knight = addCreatureReady(player1, new ExsanguinatorCavalry());
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();
        resolveAllTriggers();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Blood")).isZero();
    }
}
