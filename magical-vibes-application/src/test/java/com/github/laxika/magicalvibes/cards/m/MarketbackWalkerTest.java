package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarketbackWalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new MarketbackWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent walker = findPermanent(player1, "Marketback Walker");
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, walker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, walker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Four mana puts a +1/+1 counter on it")
    void activatedAbilityAddsCounter() {
        Permanent walker = harness.addToBattlefieldAndReturn(player1, new MarketbackWalker());
        walker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Draws one card for each +1/+1 counter when it dies")
    void drawsPerCounterOnDeath() {
        Permanent walker = harness.addToBattlefieldAndReturn(player1, new MarketbackWalker());
        walker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, walker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        harness.assertInGraveyard(player1, "Marketback Walker");
    }
}
