package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AethericAmplifier;
import com.github.laxika.magicalvibes.cards.a.ArborColossus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IchorRats;
import com.github.laxika.magicalvibes.cards.i.InstillInfection;
import com.github.laxika.magicalvibes.cards.j.JundCharm;
import com.github.laxika.magicalvibes.cards.k.KayaTheInexorable;
import com.github.laxika.magicalvibes.cards.m.MaulfistRevolutionary;
import com.github.laxika.magicalvibes.cards.m.MizzixOfTheIzmagnus;
import com.github.laxika.magicalvibes.cards.n.NuclearFallout;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SteadyProgress;
import com.github.laxika.magicalvibes.cards.t.ThrivingTurtle;
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

@CardUsed({VorinclexMonstrousRaider.class, GrizzlyBears.class, IchorRats.class,
        InstillInfection.class, JundCharm.class, KayaTheInexorable.class, ThrivingTurtle.class,
        NuclearFallout.class, ArborColossus.class, MizzixOfTheIzmagnus.class, Shock.class, SteadyProgress.class,
        MaulfistRevolutionary.class, AethericAmplifier.class})
class VorinclexMonstrousRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("doubles counters its controller puts on any permanent")
    void doublesCountersOnOpponentPermanent() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JundCharm()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, 2, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("halves counters an opponent puts on a permanent")
    void halvesCountersPutByOpponent() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new InstillInfection()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("doubles poison counters its controller puts on players")
    void doublesCountersOnPlayers() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        harness.setHand(player1, List.of(new IchorRats()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("halves poison counters an opponent puts on players")
    void halvesCountersPutByOpponentOnPlayers() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        harness.setHand(player2, List.of(new IchorRats()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void doublesEnergyCountersItsControllerReceives() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        harness.setHand(player1, List.of(new ThrivingTurtle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isEqualTo(4);
    }

    @Test
    void halvesEnergyCountersAnOpponentReceives() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        harness.setHand(player2, List.of(new ThrivingTurtle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    void doublesStartingLoyaltyAndPositiveLoyaltyCosts() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        harness.setHand(player1, List.of(new KayaTheInexorable()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castPlaneswalker(player1, 0);
        resolveAllTriggers();

        Permanent kaya = findPermanent(player1, "Kaya the Inexorable");
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(10);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(kaya),
                0, null, null);
        resolveAllTriggers();

        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(12);
    }

    @Test
    void halvesOpponentsStartingLoyaltyAndRoundsPositiveCostsDown() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        harness.setHand(player2, List.of(new KayaTheInexorable()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);

        harness.castPlaneswalker(player2, 0);
        resolveAllTriggers();

        Permanent kaya = findPermanent(player2, "Kaya the Inexorable");
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(kaya),
                0, null, null);
        resolveAllTriggers();

        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @CardUsed({VorinclexMonstrousRaider.class, KayaTheInexorable.class})
    void opposingVorinclexesAllowChoosingStartingLoyaltyReplacementOrder() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        harness.addToBattlefield(player2, new VorinclexMonstrousRaider());
        harness.setHand(player1, List.of(new KayaTheInexorable()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @CardUsed({VorinclexMonstrousRaider.class, NuclearFallout.class})
    void doublesRadCountersItsControllerPutsOnBothPlayers() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        harness.setHand(player1, List.of(new NuclearFallout()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.playerRadCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @CardUsed({VorinclexMonstrousRaider.class, NuclearFallout.class})
    void roundsDownRadCountersAnOpponentPutsOnBothPlayers() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        harness.setHand(player2, List.of(new NuclearFallout()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 1);

        assertThat(gd.playerRadCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Doubles the +1/+1 counters its controller's monstrosity puts on their own creature without halving them")
    void doublesItsControllersMonstrosityCounters() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player1, new ArborColossus());
        colossus.setSummoningSick(false);
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(colossus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Doubles the experience counters its controller gets")
    void doublesItsControllersExperienceCounters() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        harness.addToBattlefield(player1, new MizzixOfTheIzmagnus());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Doubles the poison counter its controller's proliferate gives an opponent")
    void doublesPoisonCountersItsControllerProliferates() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new SteadyProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Halves the poison counter an opponent's proliferate gives its controller")
    void halvesPoisonCountersAnOpponentProliferates() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        gd.playerPoisonCounters.put(player1.getId(), 1);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SteadyProgress()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0);
        harness.handleMultiplePermanentsChosen(player2, List.of(player1.getId()));

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Doubles the rad, spark and experience counters its controller's Maulfist Revolutionary gives an opponent")
    void doublesPlayerCountersItsControllerAddsOneOfEachKind() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        gd.playerRadCounters.put(player2.getId(), 1);
        gd.playerSparkCounters.put(player2.getId(), 1);
        gd.playerExperienceCounters.put(player2.getId(), 1);

        harness.castFromHand(player1, new MaulfistRevolutionary(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerSparkCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerExperienceCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("Doubles the counters its controller gets by doubling their own rad, spark and experience counters")
    void doublesTheCountersADoublingGivesItsController() {
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        Permanent amplifier = harness.addToBattlefieldAndReturn(player1, new AethericAmplifier());
        amplifier.setSummoningSick(false);
        gd.playerRadCounters.put(player1.getId(), 1);
        gd.playerSparkCounters.put(player1.getId(), 2);
        gd.playerExperienceCounters.put(player1.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, 1, 1, null);
        harness.passBothPriorities();

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerSparkCounters.get(player1.getId())).isEqualTo(6);
        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(9);
    }
}
