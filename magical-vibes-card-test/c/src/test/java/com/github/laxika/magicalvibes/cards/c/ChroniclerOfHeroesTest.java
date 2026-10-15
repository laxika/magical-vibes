package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SatyrRambler;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChroniclerOfHeroes.class, SatyrRambler.class, Forest.class})
class ChroniclerOfHeroesTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card when you control a creature with a +1/+1 counter")
    void etbDrawsWithCounterCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SatyrRambler());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handBefore = castChronicler();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("ETB does not draw without a matching creature")
    void etbDoesNotDrawWithoutCounterCreature() {
        int handBefore = castChronicler();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("ETB ignores an opponent's creature with a +1/+1 counter")
    void etbIgnoresOpponentCounterCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SatyrRambler());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handBefore = castChronicler();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("ETB does not draw if the matching creature loses its counter before resolution")
    void etbDoesNotDrawIfConditionChangesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SatyrRambler());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handBefore = castChronicler();

        harness.passBothPriorities();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("ETB draws when a creature gains its first counter after Chronicler enters")
    void etbDrawsWhenCounterIsAddedBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SatyrRambler());
        int handBefore = castChronicler();

        harness.passBothPriorities();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Chronicler itself can satisfy the counter condition at resolution")
    void etbDrawsWhenChroniclerGainsCounterBeforeResolution() {
        int handBefore = castChronicler();

        harness.passBothPriorities();
        Permanent chronicler = gd.playerBattlefields.get(player1.getId()).getFirst();
        chronicler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("A noncreature with a +1/+1 counter does not satisfy the condition")
    void etbIgnoresCounterOnNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handBefore = castChronicler();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Multiple qualifying creatures and counters still draw only one card")
    void etbDrawsOnlyOnceWithMultipleQualifyingCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SatyrRambler());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SatyrRambler());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handBefore = castChronicler();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    private int castChronicler() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ChroniclerOfHeroes()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        return gd.playerHands.get(player1.getId()).size();
    }
}
