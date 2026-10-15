package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MemorySluice;
import com.github.laxika.magicalvibes.cards.p.PearlOfWisdom;
import com.github.laxika.magicalvibes.cards.s.ShoreUp;
import com.github.laxika.magicalvibes.cards.t.TakeOutTheTrash;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElugeTheShorelessSea.class, Island.class, Mountain.class, Divination.class,
        PearlOfWisdom.class, ShoreUp.class, MemorySluice.class, TakeOutTheTrash.class})
class ElugeTheShorelessSeaTest extends BaseCardTest {

    @Test
    void powerAndToughnessEqualControlledIslands() {
        harness.addToBattlefield(player1, new Island());
        Permanent eluge = addCreatureReady(player1, new ElugeTheShorelessSea());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());

        assertThat(gqs.getEffectivePower(gd, eluge)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eluge)).isEqualTo(2);
    }

    @Test
    void etbFloodsLandAndIslandGrantSurvivesElugeLeavingUntilCounterIsRemoved() {
        harness.addToBattlefield(player1, new Island());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new ElugeTheShorelessSea()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, mountain.getId());
        resolveAllTriggers();

        Permanent eluge = findPermanent(player1, "Eluge, the Shoreless Sea");
        assertThat(mountain.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).contains(CardSubtype.ISLAND);

        gd.playerBattlefields.get(player1.getId()).remove(eluge);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).contains(CardSubtype.ISLAND);

        mountain.setCounterCount(CounterType.FLOOD, 0);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).doesNotContain(CardSubtype.ISLAND);
    }

    @Test
    void attacksFloodTheOnlyTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        addCreatureReady(player1, new ElugeTheShorelessSea());

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, island.getId());
        resolveAllTriggers();

        assertThat(island.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gqs.effectiveBasicLandTypes(gd, island)).contains(CardSubtype.ISLAND);
    }

    @Test
    void FirstInstantOrSorceryGetsOneReductionPerFloodedLand() {
        harness.addToBattlefield(player1, new Island());
        addCreatureReady(player1, new ElugeTheShorelessSea());
        Permanent floodedLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        floodedLand.setCounterCount(CounterType.FLOOD, 1);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Divination);

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removingLastFloodCounterPermanentlyEndsIslandGrant() {
        harness.addToBattlefield(player1, new Island());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new ElugeTheShorelessSea()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, mountain.getId());
        resolveAllTriggers();

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain))
                .contains(CardSubtype.ISLAND, CardSubtype.MOUNTAIN);
        mountain.setCounterCount(CounterType.FLOOD, 0);
        harness.runStateBasedActions();
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).doesNotContain(CardSubtype.ISLAND);

        mountain.setCounterCount(CounterType.FLOOD, 1);
        harness.runStateBasedActions();

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).doesNotContain(CardSubtype.ISLAND);
    }

    @Test
    void attackCanFloodOpponentsLandWithoutIncreasingElugesSize() {
        harness.addToBattlefield(player1, new Island());
        Permanent eluge = addCreatureReady(player1, new ElugeTheShorelessSea());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        declareAttackers(player1, List.of(1));
        harness.handlePermanentChosen(player1, mountain.getId());
        resolveAllTriggers();

        assertThat(mountain.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain))
                .contains(CardSubtype.ISLAND, CardSubtype.MOUNTAIN);
        assertThat(gqs.getEffectivePower(gd, eluge)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, eluge)).isEqualTo(1);
    }

    @Test
    void characteristicPowerAndToughnessWorkInHandAndGraveyard() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        ElugeTheShorelessSea eluge = new ElugeTheShorelessSea();
        harness.setHand(player1, List.of(eluge));

        assertThat(gqs.getEffectiveCardPower(gd, eluge)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, eluge)).isEqualTo(2);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(eluge));

        assertThat(gqs.getEffectiveCardPower(gd, eluge)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, eluge)).isEqualTo(2);
    }

    @Test
    void excessBlueReductionAlsoReducesGenericMana() {
        harness.addToBattlefield(player1, new Island());
        addCreatureReady(player1, new ElugeTheShorelessSea());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        firstLand.setCounterCount(CounterType.FLOOD, 1);
        secondLand.setCounterCount(CounterType.FLOOD, 1);
        harness.setHand(player1, List.of(new PearlOfWisdom()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Pearl of Wisdom");
    }

    @Test
    void multipleFloodCountersOnOneLandOnlyReduceCostOnce() {
        harness.addToBattlefield(player1, new Island());
        addCreatureReady(player1, new ElugeTheShorelessSea());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        land.setCounterCount(CounterType.FLOOD, 3);
        harness.setHand(player1, List.of(new PearlOfWisdom()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.assertInGraveyard(player1, "Pearl of Wisdom");
    }

    @Test
    void opponentsFloodedLandsDoNotReduceYourCosts() {
        harness.addToBattlefield(player1, new Island());
        addCreatureReady(player1, new ElugeTheShorelessSea());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        land.setCounterCount(CounterType.FLOOD, 1);
        harness.setHand(player1, List.of(new PearlOfWisdom()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void firstInstantConsumesReductionForFollowingSorcery() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.setCounterCount(CounterType.FLOOD, 1);
        Permanent eluge = addCreatureReady(player1, new ElugeTheShorelessSea());
        harness.setHand(player1, List.of(new ShoreUp()));

        harness.castAndResolveInstant(player1, 0, eluge.getId());
        harness.assertInGraveyard(player1, "Shore Up");
        harness.setHand(player1, List.of(new PearlOfWisdom()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sorceryCastBeforeElugeEntersStillConsumesFirstSpellReduction() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.setCounterCount(CounterType.FLOOD, 1);
        harness.setHand(player1, List.of(new PearlOfWisdom()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        addCreatureReady(player1, new ElugeTheShorelessSea());
        harness.setHand(player1, List.of(new PearlOfWisdom()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionCanRemoveBlueHybridManaRequirement() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.setCounterCount(CounterType.FLOOD, 1);
        addCreatureReady(player1, new ElugeTheShorelessSea());
        harness.setHand(player1, List.of(new MemorySluice()));

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Memory Sluice");
    }

    @Test
    void nonblueSpellGetsGenericReductionButStillRequiresItsOwnColor() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.setCounterCount(CounterType.FLOOD, 1);
        Permanent eluge = addCreatureReady(player1, new ElugeTheShorelessSea());
        harness.setHand(player1, List.of(new TakeOutTheTrash()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, eluge.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, eluge.getId());
        harness.assertInGraveyard(player1, "Take Out the Trash");
    }

    @Test
    void firstInstantOnOpponentsTurnAlsoGetsReduction() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.setCounterCount(CounterType.FLOOD, 1);
        Permanent eluge = addCreatureReady(player1, new ElugeTheShorelessSea());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new ShoreUp()));

        harness.castAndResolveInstant(player1, 0, eluge.getId());

        harness.assertInGraveyard(player1, "Shore Up");
    }

    @Test
    void elugeDiesWithNoIslandsBeforeItsEnterTriggerCanSaveIt() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new ElugeTheShorelessSea()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, mountain.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Eluge, the Shoreless Sea");
        assertThat(mountain.getCounterCount(CounterType.FLOOD)).isZero();
        resolveAllTriggers();
        assertThat(mountain.getCounterCount(CounterType.FLOOD)).isEqualTo(1);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain))
                .contains(CardSubtype.ISLAND, CardSubtype.MOUNTAIN);
    }
}
