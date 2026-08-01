package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeatWaveTest extends BaseCardTest {

    private Permanent addHeatWave() {
        return harness.addToBattlefieldAndReturn(player1, new HeatWave());
    }

    @Test
    @DisplayName("Blue creatures can't block creatures you control")
    void blueCreaturesCannotBlock() {
        addHeatWave();
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Blue creatures can't block creatures you control");
    }

    @Test
    @DisplayName("Nonblue creatures can block by paying 1 life each")
    void nonblueBlockCostsOneLife() {
        addHeatWave();
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ScatheZombies());
        int lifeBefore = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Each nonblue blocker costs 1 life")
    void lifeCostScalesWithBlockers() {
        addHeatWave();
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        Permanent first = addCreatureReady(player2, new ScatheZombies());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Block is rejected when the defender can't pay the life")
    void blockRejectedWithoutEnoughLife() {
        addHeatWave();
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ScatheZombies());
        gd.playerLifeTotals.put(player2.getId(), 0);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life to pay block cost (1 required)");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Restriction only covers Heat Wave's controller's creatures")
    void doesNotTaxBlocksAgainstOpponentsCreatures() {
        harness.addToBattlefield(player2, new HeatWave());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ScatheZombies());
        int lifeBefore = gd.getLife(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Heat Wave")
    void paysCumulativeUpkeep() {
        Permanent heatWave = addHeatWave();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(heatWave.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(heatWave);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Heat Wave")
    void decliningCumulativeUpkeepSacrifices() {
        Permanent heatWave = addHeatWave();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(heatWave);
        harness.assertInGraveyard(player1, "Heat Wave");
    }
}
