package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.PantherWarriors;
import com.github.laxika.magicalvibes.cards.r.RainbowEfreet;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeatWave.class, PantherWarriors.class, RainbowEfreet.class})
class HeatWaveTest extends BaseCardTest {

    private Permanent addHeatWave() {
        return harness.addToBattlefieldAndReturn(player1, new HeatWave());
    }

    @Test
    @DisplayName("Blue creatures can't block creatures you control")
    void blueCreaturesCannotBlock() {
        addHeatWave();
        addCreatureReady(player1, new PantherWarriors()).setAttacking(true);
        addCreatureReady(player2, new RainbowEfreet());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Blue creatures can't block creatures you control");
    }

    @Test
    @DisplayName("Nonblue creatures can block by paying 1 life each")
    void nonblueBlockCostsOneLife() {
        addHeatWave();
        addCreatureReady(player1, new PantherWarriors()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PantherWarriors());
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
        addCreatureReady(player1, new PantherWarriors()).setAttacking(true);
        Permanent first = addCreatureReady(player2, new PantherWarriors());
        Permanent second = addCreatureReady(player2, new PantherWarriors());
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
        addCreatureReady(player1, new PantherWarriors()).setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new PantherWarriors());
        Permanent secondBlocker = addCreatureReady(player2, new PantherWarriors());
        gd.playerLifeTotals.put(player2.getId(), 1);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life to pay block cost (2 required)");
        assertThat(firstBlocker.isBlocking()).isFalse();
        assertThat(secondBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Restriction only covers Heat Wave's controller's creatures")
    void doesNotTaxBlocksAgainstOpponentsCreatures() {
        harness.addToBattlefield(player2, new HeatWave());
        addCreatureReady(player1, new PantherWarriors()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PantherWarriors());
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
    @DisplayName("Cumulative upkeep costs two red mana on the second upkeep")
    void paysIncreasingCumulativeUpkeep() {
        Permanent heatWave = addHeatWave();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(heatWave.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(heatWave);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
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
