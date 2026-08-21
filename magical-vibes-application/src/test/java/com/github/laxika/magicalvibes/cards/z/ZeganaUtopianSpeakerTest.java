package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZeganaUtopianSpeakerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card when you control another creature with a +1/+1 counter")
    void etbDrawsWithAnotherCounteredCreature() {
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handBefore = castZegana();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("ETB does not draw without another matching creature")
    void etbDoesNotDrawWithoutAnotherMatchingCreature() {
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        int handBefore = castZegana();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("ETB ignores an opponent's creature with a +1/+1 counter")
    void etbIgnoresOpponentCounteredCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handBefore = castZegana();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Adapt 4 puts four +1/+1 counters on Zegana")
    void adaptPutsFourCountersOnZegana() {
        Permanent zegana = addZegana();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(zegana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Countered creatures you control have trample")
    void counteredOwnCreaturesHaveTrample() {
        Permanent zegana = addZegana();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        zegana.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, zegana, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample is not granted to uncountered or opposing creatures")
    void onlyCounteredOwnCreaturesHaveTrample() {
        addZegana();
        Permanent uncountered = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addZegana() {
        return addCreatureReady(player1, new ZeganaUtopianSpeaker());
    }

    private int castZegana() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new ZeganaUtopianSpeaker()));
        addCastMana();
        harness.castCreature(player1, 0);
        return gd.playerHands.get(player1.getId()).size();
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void addAdaptMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
