package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.IronshellBeetle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeastEruditeAerialist.class, IronshellBeetle.class})
class BeastEruditeAerialistTest extends BaseCardTest {

    @Test
    @DisplayName("Gains flying after receiving a +1/+1 counter this turn")
    void gainsFlyingAfterReceivingCounter() {
        Permanent beast = addCreatureReady(player1, new BeastEruditeAerialist());

        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isFalse();

        putCounterOnBeast(beast);

        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Keeps flying after its +1/+1 counter is removed this turn")
    void keepsFlyingAfterCounterIsRemoved() {
        Permanent beast = addCreatureReady(player1, new BeastEruditeAerialist());
        putCounterOnBeast(beast);

        beast.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not gain flying when an opponent puts a counter on Beast")
    void doesNotGainFlyingFromOpponentsCounter() {
        Permanent beast = addCreatureReady(player1, new BeastEruditeAerialist());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new IronshellBeetle()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0, beast.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses flying next turn even while the counter remains")
    void losesFlyingNextTurn() {
        Permanent beast = addCreatureReady(player1, new BeastEruditeAerialist());
        putCounterOnBeast(beast);
        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Draws a card when dealing combat damage to a player")
    void drawsOnCombatDamageToPlayer() {
        Permanent beast = addCreatureReady(player1, new BeastEruditeAerialist());
        beast.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The opponent draws when their Beast deals combat damage")
    void drawsForOpposingController() {
        Permanent beast = addCreatureReady(player2, new BeastEruditeAerialist());
        beast.setAttacking(true);
        int controllerHandSize = gd.playerHands.get(player2.getId()).size();
        int defendingHandSize = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(controllerHandSize + 1);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(defendingHandSize);
    }

    @Test
    @DisplayName("Does not draw when blocked and no combat damage reaches a player")
    void doesNotDrawWhenBlocked() {
        Permanent beast = addCreatureReady(player1, new BeastEruditeAerialist());
        beast.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new IronshellBeetle());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    private void putCounterOnBeast(Permanent beast) {
        harness.setHand(player1, List.of(new IronshellBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, beast.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
