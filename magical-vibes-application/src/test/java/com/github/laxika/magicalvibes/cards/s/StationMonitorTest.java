package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

@CardUsed({StationMonitor.class, LightningBolt.class, GrizzlyBears.class, EkunduGriffin.class})
class StationMonitorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Drone for the second spell each turn")
    void createsDroneForSecondSpellEachTurn() {
        harness.addToBattlefield(player1, new StationMonitor());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Drone")).isZero();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Drone")).isEqualTo(1);
        Permanent drone = findPermanent(player1, "Drone");
        assertThat(drone.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, drone)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drone)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, drone, Keyword.FLYING)).isTrue();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
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

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Drone")).isZero();
    }

    @Test
    @DisplayName("A created Drone can block flying creatures but not ground creatures")
    void droneCanBlockOnlyFlyingCreatures() {
        harness.addToBattlefield(player1, new StationMonitor());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent groundAttacker = addCreatureReady(player2, new GrizzlyBears());
        Permanent flyingAttacker = addCreatureReady(player2, new EkunduGriffin());
        Permanent drone = findPermanent(player1, "Drone");

        declareAttackers(player2, List.of(0, 1));
        prepareDeclareBlockers(player2);

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
