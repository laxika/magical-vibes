package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.IronshellBeetle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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
        Permanent beast = addReadyBeast();

        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isFalse();

        putCounterOnBeast(beast);

        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Keeps flying after its +1/+1 counter is removed this turn")
    void keepsFlyingAfterCounterIsRemoved() {
        Permanent beast = addReadyBeast();
        putCounterOnBeast(beast);

        beast.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, beast, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Draws a card when dealing combat damage to a player")
    void drawsOnCombatDamageToPlayer() {
        Permanent beast = addReadyBeast();
        beast.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not draw when blocked and no combat damage reaches a player")
    void doesNotDrawWhenBlocked() {
        Permanent beast = addReadyBeast();
        beast.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new IronshellBeetle());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    private Permanent addReadyBeast() {
        Permanent beast = harness.addToBattlefieldAndReturn(player1, new BeastEruditeAerialist());
        beast.setSummoningSick(false);
        return beast;
    }

    private void putCounterOnBeast(Permanent beast) {
        harness.setHand(player1, List.of(new IronshellBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gs.playCard(gd, player1, 0, 0, beast.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
