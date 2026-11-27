package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AethericAmplifier;
import com.github.laxika.magicalvibes.cards.a.AetherMeltdown;
import com.github.laxika.magicalvibes.cards.b.BloatflySwarm;
import com.github.laxika.magicalvibes.cards.b.BloodPoet;
import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.c.CaressOfPhyrexia;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.MariposaMilitaryBase;
import com.github.laxika.magicalvibes.cards.m.MaulfistRevolutionary;
import com.github.laxika.magicalvibes.cards.m.MizzixOfTheIzmagnus;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.n.NuclearFallout;
import com.github.laxika.magicalvibes.cards.r.RealityRipple;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Suncleanser.class, GreenwoodSentinel.class, BurstOfStrength.class, Murder.class,
        CaressOfPhyrexia.class, AetherMeltdown.class, RealityRipple.class, NuclearFallout.class,
        SteadyProgress.class, MaulfistRevolutionary.class, BloatflySwarm.class, Shock.class, MariposaMilitaryBase.class,
        AethericAmplifier.class, BloodPoet.class, MizzixOfTheIzmagnus.class})
class SuncleanserTest extends BaseCardTest {

    private static final String CREATURE_MODE =
            "Remove all counters from target creature. It can't have counters put on it for as long as this creature remains on the battlefield.";
    private static final String PLAYER_MODE =
            "Target opponent loses all counters. That player can't get counters for as long as this creature remains on the battlefield.";

    @Nested
    @CardUsed({Suncleanser.class, GreenwoodSentinel.class, BurstOfStrength.class, Murder.class, RealityRipple.class})
    @DisplayName("Mode 1: remove all counters from target creature and lock it")
    class CreatureMode {

        @Test
        @DisplayName("Removes every counter from the targeted creature")
        void removesAllCounters() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
            bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
            bears.setCounterCount(CounterType.CHARGE, 1);

            castWithCreatureMode(bears.getId());
            harness.passBothPriorities(); // resolve the ETB trigger

            assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(bears.getCounterCount(CounterType.CHARGE)).isZero();
        }

        @Test
        @DisplayName("The locked creature can't have counters put on it afterwards")
        void lockedCreatureGetsNoNewCounters() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

            castWithCreatureMode(bears.getId());
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new BurstOfStrength()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.castAndResolveInstant(player1, 0, bears.getId());

            assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        }

        @Test
        @DisplayName("The lock ends when Suncleanser leaves the battlefield")
        void lockEndsWhenSuncleanserLeaves() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

            castWithCreatureMode(bears.getId());
            harness.passBothPriorities();

            Permanent suncleanser = findPermanent(player1, "Suncleanser");
            harness.setHand(player2, List.of(new Murder()));
            harness.addMana(player2, ManaColor.BLACK, 3);
            harness.castAndResolveInstant(player2, 0, suncleanser.getId());

            harness.setHand(player1, List.of(new BurstOfStrength()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.castAndResolveInstant(player1, 0, bears.getId());

            assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

        @Test
        @DisplayName("The lock ends for good as Suncleanser phases out; it doesn't come back as Suncleanser phases in")
        void lockEndsForGoodAsSuncleanserPhasesOut() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

            castWithCreatureMode(bears.getId());
            harness.passBothPriorities();

            Permanent suncleanser = findPermanent(player1, "Suncleanser");
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player1, List.of(new RealityRipple()));
            harness.addMana(player1, ManaColor.BLUE, 2);
            harness.castAndResolveInstant(player1, 0, suncleanser.getId());
            harness.performUntapStep(player1);
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(suncleanser);

            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player1, List.of(new BurstOfStrength()));
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.castAndResolveInstant(player1, 0, bears.getId());

            assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

        @Test
        void removesCountersButDoesNotLockIfSourceLeavesBeforeTriggerResolves() {
            Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
            creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
            castWithCreatureMode(creature.getId());

            Permanent source = findPermanent(player1, "Suncleanser");
            harness.setHand(player2, List.of(new Murder()));
            harness.addMana(player2, ManaColor.BLACK, 3);
            harness.castAndResolveInstant(player2, 0, source.getId());
            harness.passBothPriorities();

            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            harness.setHand(player1, List.of(new BurstOfStrength()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.castAndResolveInstant(player1, 0, creature.getId());
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        }

        private void castWithCreatureMode(UUID targetId) {
            harness.setHand(player1, List.of(new Suncleanser()));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            harness.handleListChoice(player1, CREATURE_MODE);
            harness.handlePermanentChosen(player1, targetId);
        }
    }

    @Nested
    @CardUsed({Suncleanser.class, CaressOfPhyrexia.class, Murder.class, AetherMeltdown.class, NuclearFallout.class})
    @DisplayName("Mode 2: target opponent loses all counters and is locked")
    class PlayerMode {

        @Test
        @DisplayName("Clears the targeted opponent's poison counters")
        void clearsPoisonCounters() {
            gd.playerPoisonCounters.put(player2.getId(), 3);

            castWithPlayerMode();
            harness.passBothPriorities(); // resolve the ETB trigger

            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        }

        @Test
        @DisplayName("The locked opponent can't get poison counters afterwards")
        void lockedPlayerGetsNoNewCounters() {
            castWithPlayerMode();
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new CaressOfPhyrexia()));
            harness.addMana(player1, ManaColor.BLACK, 5);
            harness.castAndResolveSorcery(player1, 0, player2.getId());

            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        }

        @Test
        @DisplayName("The controller is unaffected by the opponent's lock")
        void controllerStillGetsCounters() {
            castWithPlayerMode();
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new CaressOfPhyrexia()));
            harness.addMana(player1, ManaColor.BLACK, 5);
            harness.castAndResolveSorcery(player1, 0, player1.getId());

            assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(3);
        }

        @Test
        void removesEverySupportedPlayerCounterType() {
            gd.playerPoisonCounters.put(player2.getId(), 3);
            gd.playerEnergyCounters.put(player2.getId(), 4);
            gd.playerExperienceCounters.put(player2.getId(), 5);
            gd.playerRadCounters.put(player2.getId(), 2);
            gd.playerSparkCounters.put(player2.getId(), 1);

            castWithPlayerMode();
            harness.passBothPriorities();

            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
            assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
            assertThat(gd.playerExperienceCounters.getOrDefault(player2.getId(), 0)).isZero();
            assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
            assertThat(gd.playerSparkCounters.getOrDefault(player2.getId(), 0)).isZero();
        }

        @Test
        void lockedOpponentCannotGetEnergyCounters() {
            castWithPlayerMode();
            harness.passBothPriorities();

            Permanent source = findPermanent(player1, "Suncleanser");
            harness.setHand(player2, List.of(new AetherMeltdown()));
            harness.addMana(player2, ManaColor.BLUE, 2);
            harness.castEnchantment(player2, 0, source.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
        }

        @Test
        void opponentCanGetPoisonAgainAfterSourceLeaves() {
            castWithPlayerMode();
            harness.passBothPriorities();

            Permanent source = findPermanent(player1, "Suncleanser");
            harness.setHand(player2, List.of(new Murder()));
            harness.addMana(player2, ManaColor.BLACK, 3);
            harness.castAndResolveInstant(player2, 0, source.getId());

            harness.setHand(player1, List.of(new CaressOfPhyrexia()));
            harness.addMana(player1, ManaColor.BLACK, 5);
            harness.castAndResolveSorcery(player1, 0, player2.getId());
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
        }

        @Test
        @DisplayName("The locked opponent can't get rad counters; its controller still gets them")
        void lockedOpponentCannotGetRadCounters() {
            castWithPlayerMode();
            harness.passBothPriorities();

            harness.setHand(player1, List.of(new NuclearFallout()));
            harness.addMana(player1, ManaColor.BLACK, 3);
            harness.castAndResolveSorcery(player1, 0, 1);

            assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
            assertThat(gd.playerRadCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        }

        private void castWithPlayerMode() {
            harness.setHand(player1, List.of(new Suncleanser()));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            harness.handleListChoice(player1, PLAYER_MODE);
            harness.handlePermanentChosen(player1, player2.getId());
        }
    }

    @Nested
    @CardUsed({Suncleanser.class, SteadyProgress.class, MaulfistRevolutionary.class, BloatflySwarm.class, Shock.class,
            MariposaMilitaryBase.class, AethericAmplifier.class, BloodPoet.class, MizzixOfTheIzmagnus.class})
    @DisplayName("Every way of giving a player counters respects the player lock")
    class PlayerLockOnEveryWriter {

        @Test
        @DisplayName("Proliferate gives the locked opponent no experience, energy, rad or poison counter")
        void proliferateGivesNoCounters() {
            lockOpponent();
            gd.playerExperienceCounters.put(player2.getId(), 1);
            gd.playerEnergyCounters.put(player2.getId(), 1);
            gd.playerRadCounters.put(player2.getId(), 1);
            gd.playerPoisonCounters.put(player2.getId(), 1);
            harness.setHand(player1, List.of(new SteadyProgress()));
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.castAndResolveInstant(player1, 0);
            harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

            assertThat(gd.playerExperienceCounters.get(player2.getId())).isEqualTo(1);
            assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(1);
            assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(1);
            assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        }

        @Test
        @DisplayName("Maulfist Revolutionary gives the locked opponent no rad, spark or experience counter")
        void anotherCounterOfEachKindGivesNoCounters() {
            lockOpponent();
            gd.playerRadCounters.put(player2.getId(), 1);
            gd.playerSparkCounters.put(player2.getId(), 1);
            gd.playerExperienceCounters.put(player2.getId(), 1);

            harness.castFromHand(player1, new MaulfistRevolutionary(), "{1}{G}{G}");
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(1);
            assertThat(gd.playerSparkCounters.get(player2.getId())).isEqualTo(1);
            assertThat(gd.playerExperienceCounters.get(player2.getId())).isEqualTo(1);
        }

        @Test
        @DisplayName("Bloatfly Swarm's prevention gives the locked opponent no rad counters; its controller still gets them")
        void bloatflySwarmGivesTheLockedOpponentNoRadCounters() {
            lockOpponent();
            Permanent swarm = harness.addToBattlefieldAndReturn(player1, new BloatflySwarm());
            swarm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castAndResolveInstant(player1, 0, swarm.getId());

            assertThat(gd.playerRadCounters.getOrDefault(player1.getId(), 0)).isEqualTo(2);
            assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
        }

        @Test
        @DisplayName("The locked opponent gets no rad counters for having Mariposa Military Base enter tapped")
        void mariposaMilitaryBaseGivesNoRadCounters() {
            lockOpponent();
            asOpponentsMainPhase();
            harness.setHand(player2, List.of(new MariposaMilitaryBase()));

            harness.playLand(player2, 0);
            harness.handleMayAbilityChosen(player2, true);

            assertThat(findPermanent(player2, "Mariposa Military Base").isTapped()).isTrue();
            assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
        }

        @Test
        @DisplayName("Doubling the locked opponent's counters gives them no rad, spark or experience counters")
        void doublingGivesNoCounters() {
            lockOpponent();
            Permanent amplifier = harness.addToBattlefieldAndReturn(player2, new AethericAmplifier());
            amplifier.setSummoningSick(false);
            gd.playerRadCounters.put(player2.getId(), 1);
            gd.playerSparkCounters.put(player2.getId(), 2);
            gd.playerExperienceCounters.put(player2.getId(), 3);
            asOpponentsMainPhase();
            harness.addMana(player2, ManaColor.COLORLESS, 4);

            harness.activateAbility(player2, 0, 1, 1, null);
            harness.passBothPriorities();

            assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(1);
            assertThat(gd.playerSparkCounters.get(player2.getId())).isEqualTo(2);
            assertThat(gd.playerExperienceCounters.get(player2.getId())).isEqualTo(3);
        }

        @Test
        @DisplayName("The locked opponent can't pay a spark cost that puts spark counters on them")
        void lockedOpponentCantPayAPlusSparkCost() {
            lockOpponent();
            harness.addToBattlefield(player2, new BloodPoet());
            asOpponentsMainPhase();

            assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(gd.playerSparkCounters.getOrDefault(player2.getId(), 0)).isZero();
        }

        @Test
        @DisplayName("The locked opponent gets no experience counters")
        void lockedOpponentGetsNoExperienceCounters() {
            lockOpponent();
            harness.addToBattlefield(player2, new MizzixOfTheIzmagnus());
            asOpponentsMainPhase();
            harness.setHand(player2, List.of(new Shock()));
            harness.addMana(player2, ManaColor.RED, 1);

            harness.castInstant(player2, 0, player1.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(gd.playerExperienceCounters.getOrDefault(player2.getId(), 0)).isZero();
        }

        private void lockOpponent() {
            harness.setHand(player1, List.of(new Suncleanser()));
            harness.addMana(player1, ManaColor.WHITE, 2);
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            harness.handleListChoice(player1, PLAYER_MODE);
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();
        }

        private void asOpponentsMainPhase() {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
        }
    }
}
