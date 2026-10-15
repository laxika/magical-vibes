package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AetherVial;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GavonyTownship;
import com.github.laxika.magicalvibes.cards.h.HopefulInitiate;
import com.github.laxika.magicalvibes.cards.h.HamletVanguard;
import com.github.laxika.magicalvibes.cards.s.SporebackWolf;
import com.github.laxika.magicalvibes.cards.t.TravelPreparations;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloakedCadet.class, AirElemental.class, GavonyTownship.class, Forest.class,
        HopefulInitiate.class, Coretapper.class, AetherVial.class, HamletVanguard.class,
        SporebackWolf.class, TravelPreparations.class})
class CloakedCadetTest extends BaseCardTest {

    @Test
    void trainingPutsACounterOnCloakedCadet() {
        Permanent cadet = addCreatureReady(player1, new CloakedCadet());
        Permanent airElemental = addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(cadet),
                gd.playerBattlefields.get(player1.getId()).indexOf(airElemental)));
        harness.passBothPriorities();

        assertThat(cadet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void drawsOnlyOnceWhenControlledHumanGetsCounters() {
        Permanent cadet = addCreatureReady(player1, new CloakedCadet());
        Permanent firstTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        Permanent secondTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        activateTownship(player1, firstTownship);
        activateTownship(player1, secondTownship);

        assertThat(cadet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(Forest.class);
    }

    @Test
    void doesNotTriggerForHumanControlledByOpponent() {
        addCreatureReady(player1, new CloakedCadet());
        Permanent township = harness.addToBattlefieldAndReturn(player2, new GavonyTownship());
        Permanent hopefulInitiate = addCreatureReady(player2, new HopefulInitiate());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        activateTownship(player2, township);

        assertThat(hopefulInitiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerForNonPlusOneCounters() {
        addCreatureReady(player1, new CloakedCadet());
        Permanent coretapper = addCreatureReady(player1, new Coretapper());
        Permanent vial = harness.addToBattlefieldAndReturn(player1, new AetherVial());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(coretapper), 0, null, vial.getId());
        harness.passBothPriorities();

        assertThat(vial.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void trainingAlsoDrawsACard() {
        Permanent cadet = addCreatureReady(player1, new CloakedCadet());
        Permanent vanguard = addCreatureReady(player1, new HamletVanguard());
        vanguard.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(cadet),
                gd.playerBattlefields.get(player1.getId()).indexOf(vanguard)));
        resolveAllTriggers();

        assertThat(cadet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void trainingDoesNotTriggerWithOnlyEqualPowerAttackers() {
        Permanent cadet = addCreatureReady(player1, new CloakedCadet());
        Permanent wolf = addCreatureReady(player1, new SporebackWolf());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(cadet),
                gd.playerBattlefields.get(player1.getId()).indexOf(wolf)));
        resolveAllTriggers();

        assertThat(cadet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void simultaneousCountersOnMultipleHumansDrawOnlyOneCard() {
        Permanent cadet = addCreatureReady(player1, new CloakedCadet());
        Permanent initiate = addCreatureReady(player1, new HopefulInitiate());
        Permanent township = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        activateTownship(player1, township);

        assertThat(cadet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(initiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void eachCadetHasItsOwnOncePerTurnLimit() {
        addCreatureReady(player1, new CloakedCadet());
        addCreatureReady(player1, new CloakedCadet());
        Permanent firstTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        Permanent secondTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        activateTownship(player1, firstTownship);
        activateTownship(player1, secondTownship);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void humanEnteringWithCountersDrawsOneCard() {
        addCreatureReady(player1, new CloakedCadet());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.castFromHand(player1, new HamletVanguard(), "{2}{G}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Hamlet Vanguard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void nonHumanCountersDoNotConsumeTheHumanTrigger() {
        Permanent cadet = addCreatureReady(player1, new CloakedCadet());
        Permanent wolf = addCreatureReady(player1, new SporebackWolf());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new TravelPreparations(), new TravelPreparations()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(wolf.getId()));
        resolveAllTriggers();

        assertThat(wolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(TravelPreparations.class);

        harness.castAndResolveSorcery(player1, 0, List.of(cadet.getId()));
        resolveAllTriggers();

        assertThat(cadet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .first().isInstanceOf(Forest.class);
    }

    @Test
    void canTriggerAgainDuringTheOpponentsNextTurn() {
        Permanent cadet = addCreatureReady(player1, new CloakedCadet());
        Permanent firstTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        Permanent secondTownship = harness.addToBattlefieldAndReturn(player1, new GavonyTownship());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        activateTownship(player1, firstTownship);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(secondTownship), 1, null, null);
        resolveAllTriggers();

        assertThat(cadet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void activateTownship(Player player, Permanent township) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.activateAbility(player,
                gd.playerBattlefields.get(player.getId()).indexOf(township), 1, null, null);
        resolveAllTriggers();
    }
}
