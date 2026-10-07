package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StationMonitor.class, LightningBolt.class})
class StationMonitorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Drone for the second spell each turn")
    void createsDroneForSecondSpellEachTurn() {
        harness.addToBattlefield(player1, new StationMonitor());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Drone")).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(countPermanents(player1, "Drone")).isEqualTo(1);
        Permanent drone = findPermanent(player1, "Drone");
        assertThat(drone.getCard().isToken()).isTrue();
        assertThat(drone.getCard().getColors()).isEmpty();
        assertThat(drone.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(drone.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(drone.getCard().getSubtypes()).containsExactly(CardSubtype.DRONE);
        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, drone, Keyword.FLYING)).isTrue();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Drone")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's second spell")
    void doesNotTriggerForOpponentsSpell() {
        harness.addToBattlefield(player1, new StationMonitor());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(countPermanents(player1, "Drone")).isZero();
    }

    @Test
    void countsStationMonitorCastBeforeItEnteredTheBattlefield() {
        harness.setHand(player1, List.of(new StationMonitor(), new LightningBolt()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Drone")).isZero();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Drone")).isEqualTo(1);
    }

    @Test
    @CardUsed({StationMonitor.class})
    void doesNotTriggerForItsOwnCastAsSecondSpell() {
        harness.setHand(player1, List.of(new StationMonitor(), new StationMonitor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Drone")).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Station Monitor")).isEqualTo(2);
    }

    @Test
    void triggersForControllersSecondSpellDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new StationMonitor());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Drone")).isZero();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(countPermanents(player1, "Drone")).isEqualTo(1);
    }

    @Test
    void triggerStillResolvesAfterMonitorIsDestroyed() {
        harness.addToBattlefield(player1, new StationMonitor());
        Permanent monitor = findPermanent(player1, "Station Monitor");
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player2, 0, monitor.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Station Monitor");
        assertThat(countPermanents(player1, "Drone")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Drone")).isEqualTo(1);
    }

    @Test
    void canTriggerAgainOnTheNextTurn() {
        harness.addToBattlefield(player1, new StationMonitor());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(),
                new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Drone")).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Drone")).isEqualTo(1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(countPermanents(player1, "Drone")).isEqualTo(2);
    }

    @Test
    @CardUsed({StationMonitor.class, LightningBolt.class, GrizzlyBears.class, EkunduGriffin.class})
    @DisplayName("A created Drone can block flying creatures but not ground creatures")
    void droneCanBlockOnlyFlyingCreatures() {
        harness.addToBattlefield(player1, new StationMonitor());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent groundAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent flyingAttacker = addCreatureReady(player2, new EkunduGriffin());
        Permanent drone = findPermanent(player1, "Drone");

        declareAttackersAndPrepareBlockers(player2, List.of(0, 1));

        int droneIndex = gd.playerBattlefields.get(player1.getId()).indexOf(drone);
        int groundAttackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(groundAttacker);
        int flyingAttackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(flyingAttacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(droneIndex, groundAttackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(droneIndex, flyingAttackerIndex)));
        assertThat(drone.isBlocking()).isTrue();
    }
}
