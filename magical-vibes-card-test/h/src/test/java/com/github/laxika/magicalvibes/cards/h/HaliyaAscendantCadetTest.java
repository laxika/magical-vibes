package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaliyaAscendantCadet.class, Forest.class, GrizzlyBears.class})
class HaliyaAscendantCadetTest extends BaseCardTest {

    @Test
    void entersAndAttacksWithTargetedCounterTriggers() {
        harness.castFromHand(player1, new HaliyaAscendantCadet(), "{2}{G}{W}{W}");
        harness.passBothPriorities();
        Permanent haliya = findPermanent(player1, "Haliya, Ascendant Cadet");
        harness.handlePermanentChosen(player1, haliya.getId());
        harness.passBothPriorities();

        assertThat(haliya.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        haliya.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, haliya.getId());
        harness.passBothPriorities();

        assertThat(haliya.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void drawsOnceWhenOneOrMoreCounteredCreaturesDealCombatDamage() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        addCreatureReady(player1, new HaliyaAscendantCadet());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        firstAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        secondAttacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void creaturesWithoutPlusOneCountersDoNotTriggerTheDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        addCreatureReady(player1, new HaliyaAscendantCadet());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void counterTriggersCanTargetAnotherCreatureYouControl() {
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new HaliyaAscendantCadet(), "{2}{G}{W}{W}");
        harness.passBothPriorities();
        Permanent haliya = findPermanent(player1, "Haliya, Ascendant Cadet");
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(haliya.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        haliya.setSummoningSick(false);
        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(haliya.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void haliyasOwnCombatDamageCanTriggerTheDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent haliya = addCreatureReady(player1, new HaliyaAscendantCadet());
        haliya.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        haliya.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsCounteredCreatureDoesNotDrawForHaliya() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new HaliyaAscendantCadet());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void otherCounterTypesDoNotTriggerTheDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new HaliyaAscendantCadet());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
