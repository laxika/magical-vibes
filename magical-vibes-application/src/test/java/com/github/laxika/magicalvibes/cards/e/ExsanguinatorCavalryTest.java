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
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        resolveCombat();
        resolveAllTriggers();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Blood")).isZero();
    }

    @Test
    @DisplayName("Cavalry triggers for its own combat damage and gains life through lifelink")
    void ownCombatDamageTriggers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent cavalry = addCreatureReady(player1, new ExsanguinatorCavalry());
        cavalry.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Each Cavalry triggers separately for each Knight dealing combat damage")
    void multipleCavalriesAndKnightsTriggerSeparately() {
        Permanent first = addCreatureReady(player1, new ExsanguinatorCavalry());
        Permanent second = addCreatureReady(player1, new ExsanguinatorCavalry());
        first.setAttacking(true);
        second.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Blood")).isEqualTo(4);
    }

    @Test
    @DisplayName("An opposing Knight dealing combat damage does not trigger Cavalry")
    void opposingKnightDoesNotTrigger() {
        addCreatureReady(player1, new ExsanguinatorCavalry());
        Permanent knight = addCreatureReady(player2, new YouthfulKnight());
        knight.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(knight.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Blood")).isZero();
        assertThat(countPermanents(player2, "Blood")).isZero();
    }

    @Test
    @DisplayName("Blood is still created if the damaging Knight leaves before resolution")
    void bloodCreatedAfterKnightLeaves() {
        Permanent cavalry = addCreatureReady(player1, new ExsanguinatorCavalry());
        cavalry.setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(cavalry);
        gd.playerGraveyards.get(player1.getId()).add(cavalry.getCard());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }
}
