package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
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

@CardUsed({ZeganaUtopianSpeaker.class, SauroformHybrid.class})
class ZeganaUtopianSpeakerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card when you control another creature with a +1/+1 counter")
    void etbDrawsWithAnotherCounteredCreature() {
        Permanent other = addCreatureReady(player1, new SauroformHybrid());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handBefore = castZegana();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("ETB does not draw without another matching creature")
    void etbDoesNotDrawWithoutAnotherMatchingCreature() {
        Permanent other = addCreatureReady(player1, new SauroformHybrid());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        int handBefore = castZegana();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("ETB ignores an opponent's creature with a +1/+1 counter")
    void etbIgnoresOpponentCounteredCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new SauroformHybrid());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handBefore = castZegana();

        resolveAllTriggers();

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
        Permanent creature = addCreatureReady(player1, new SauroformHybrid());
        zegana.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, zegana, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample is not granted to uncountered or opposing creatures")
    void onlyCounteredOwnCreaturesHaveTrample() {
        addZegana();
        Permanent uncountered = addCreatureReady(player1, new SauroformHybrid());
        Permanent opponentCreature = addCreatureReady(player2, new SauroformHybrid());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, uncountered, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("ETB condition is checked again when the draw trigger resolves")
    void etbDoesNotDrawAfterOtherCreatureLosesCounters() {
        Permanent other = addCreatureReady(player1, new SauroformHybrid());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handBefore = castZegana();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        findPermanent(player1, "Zegana, Utopian Speaker")
                .setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("A false ETB condition does not trigger even if counters are added later")
    void etbDoesNotTriggerWhenConditionInitiallyFalse() {
        Permanent other = addCreatureReady(player1, new SauroformHybrid());
        int handBefore = castZegana();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Adapt may be activated with counters but does not add more")
    void adaptDoesNothingWithExistingCounter() {
        Permanent zegana = addZegana();
        zegana.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(zegana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two adapt abilities on the stack only add counters once")
    void stackedAdaptAbilitiesOnlyAddFourCounters() {
        Permanent zegana = addZegana();
        addAdaptMana();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(zegana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, zegana, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Adapt checks for counters at resolution")
    void adaptDoesNothingIfCounterAddedInResponse() {
        Permanent zegana = addZegana();
        addAdaptMana();
        harness.activateAbility(player1, 0, null, null);

        zegana.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(zegana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adapt can add counters again after all previous counters are removed")
    void adaptWorksAgainAfterCountersRemoved() {
        Permanent zegana = addZegana();
        zegana.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        addAdaptMana();
        harness.activateAbility(player1, 0, null, null);

        zegana.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();

        assertThat(zegana.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Trample is gained and lost as the last +1/+1 counter changes")
    void trampleTracksCounters() {
        addZegana();
        Permanent creature = addCreatureReady(player1, new SauroformHybrid());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample is lost when Zegana leaves the battlefield")
    void trampleEndsWhenZeganaLeaves() {
        Permanent zegana = addZegana();
        Permanent creature = addCreatureReady(player1, new SauroformHybrid());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(zegana);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addZegana() {
        return addCreatureReady(player1, new ZeganaUtopianSpeaker());
    }

    private int castZegana() {
        harness.setLibrary(player1, List.of(new SauroformHybrid(), new SauroformHybrid()));
        harness.castFromHand(player1, new ZeganaUtopianSpeaker(), "{2}{G}{U}");
        return gd.playerHands.get(player1.getId()).size();
    }

    private void addAdaptMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
