package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FellGravship;
import com.github.laxika.magicalvibes.cards.c.ContentiousPlan;
import com.github.laxika.magicalvibes.cards.k.KavaronMemorialWorld;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PyramidOfThePantheon;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoadingZone.class, FellGravship.class, GrizzlyBears.class,
        PyramidOfThePantheon.class, TimberlandGuide.class, ContentiousPlan.class,
        KavaronMemorialWorld.class, SongOfTheDryads.class})
class LoadingZoneTest extends BaseCardTest {

    @Test
    @DisplayName("doubles counters put on a controlled creature")
    void doublesCountersOnControlledCreature() {
        harness.addToBattlefield(player1, new LoadingZone());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, List.of(bears.getId()));
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("doubles counters put on a controlled Spacecraft")
    void doublesCountersOnControlledSpacecraft() {
        harness.addToBattlefield(player1, new LoadingZone());
        Permanent gravship = harness.addToBattlefieldAndReturn(player1, new FellGravship());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(gravship), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gravship.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
    }

    @Test
    @DisplayName("does not double counters put on another controlled permanent")
    void doesNotDoubleCountersOnOtherPermanents() {
        harness.addToBattlefield(player1, new LoadingZone());
        Permanent pyramid = harness.addToBattlefieldAndReturn(player1, new PyramidOfThePantheon());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(pyramid), 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(pyramid.getCounterCount(CounterType.BRICK)).isEqualTo(1);
    }

    @Test
    void doublesCountersOnControlledPlanet() {
        harness.addToBattlefield(player1, new LoadingZone());
        Permanent planet = harness.addToBattlefieldAndReturn(player1, new KavaronMemorialWorld());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, battlefieldIndex(planet), 1, null, null);
        resolveAllTriggers();
        assertThat(planet.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    void doesNotDoubleCountersOnOpponentsCreature() {
        harness.addToBattlefield(player1, new LoadingZone());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, List.of(bears.getId()));
        resolveAllTriggers();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void twoLoadingZonesQuadrupleCounters() {
        harness.addToBattlefield(player1, new LoadingZone());
        harness.addToBattlefield(player1, new LoadingZone());
        Permanent planet = harness.addToBattlefieldAndReturn(player1, new KavaronMemorialWorld());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, battlefieldIndex(planet), 1, null, null);
        resolveAllTriggers();
        assertThat(planet.getCounterCount(CounterType.CHARGE)).isEqualTo(8);
    }

    @Test
    void doublesEachCounterKindAddedByProliferate() {
        harness.addToBattlefield(player1, new LoadingZone());
        Permanent gravship = harness.addToBattlefieldAndReturn(player1, new FellGravship());
        gravship.setCounterCount(CounterType.CHARGE, 1);
        gravship.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        proliferate(gravship);
        assertThat(gravship.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gravship.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void doesNotDoubleCountersOnSpacecraftTurnedIntoForest() {
        harness.addToBattlefield(player1, new LoadingZone());
        Permanent gravship = harness.addToBattlefieldAndReturn(player1, new FellGravship());
        gravship.setCounterCount(CounterType.CHARGE, 1);
        turnIntoForest(gravship);
        proliferate(gravship);
        assertThat(gravship.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void doesNotDoubleCountersOnCreatureTurnedIntoForest() {
        harness.addToBattlefield(player1, new LoadingZone());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        turnIntoForest(bears);
        proliferate(bears);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void warpedEnchantmentExilesThenCanBeCastOnLaterTurn() {
        LoadingZone zone = new LoadingZone();
        harness.setHand(player1, List.of(zone));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Loading Zone");
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Loading Zone");
        assertThat(gd.findExiledCard(zone.getId())).isNotNull();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, zone.getId());
        resolveAllTriggers();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Loading Zone");
        assertThat(gd.findExiledCard(zone.getId())).isNull();
    }

    @Test
    void doesNotDoubleCountersOnPlanetTurnedIntoForest() {
        harness.addToBattlefield(player1, new LoadingZone());
        Permanent planet = harness.addToBattlefieldAndReturn(player1, new KavaronMemorialWorld());
        planet.setCounterCount(CounterType.CHARGE, 1);
        turnIntoForest(planet);
        proliferate(planet);
        assertThat(planet.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void doesNotDoubleCountersOnOpponentsSpacecraftOrPlanet() {
        harness.addToBattlefield(player1, new LoadingZone());
        Permanent gravship = harness.addToBattlefieldAndReturn(player2, new FellGravship());
        Permanent planet = harness.addToBattlefieldAndReturn(player2, new KavaronMemorialWorld());
        gravship.setCounterCount(CounterType.CHARGE, 1);
        planet.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of(new ContentiousPlan()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(gravship.getId(), planet.getId()));
        assertThat(gravship.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(planet.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void normalCastDoesNotExileAtEndStep() {
        LoadingZone zone = new LoadingZone();
        harness.setHand(player1, List.of(zone));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Loading Zone");
        assertThat(gd.findExiledCard(zone.getId())).isNull();
    }

    private void turnIntoForest(Permanent permanent) {
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, permanent.getId());
        resolveAllTriggers();
    }

    private void proliferate(Permanent permanent) {
        harness.setHand(player1, List.of(new ContentiousPlan()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(permanent.getId()));
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
