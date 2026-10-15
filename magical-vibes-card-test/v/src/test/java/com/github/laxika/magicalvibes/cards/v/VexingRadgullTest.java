package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VexingRadgull.class)
class VexingRadgullTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage gives two rad counters to a player without rad counters")
    void combatDamageGivesTwoRadCountersWithoutExistingRadCounters() {
        Permanent radgull = addCreatureReady(player1, new VexingRadgull());
        radgull.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage proliferates a player's existing rad counters")
    void combatDamageProliferatesExistingRadCounters() {
        gd.playerRadCounters.put(player2.getId(), 1);
        Permanent radgull = addCreatureReady(player1, new VexingRadgull());
        radgull.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferating may choose no permanents or players")
    void proliferateMayChooseNothing() {
        gd.playerRadCounters.put(player2.getId(), 1);
        Permanent radgull = addCreatureReady(player1, new VexingRadgull());
        radgull.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Proliferate can choose other players and permanents without choosing the damaged player")
    void proliferateCanChooseOtherObjects() {
        gd.playerRadCounters.put(player2.getId(), 1);
        gd.playerRadCounters.put(player1.getId(), 2);
        gd.playerEnergyCounters.put(player1.getId(), 3);
        Permanent radgull = addCreatureReady(player1, new VexingRadgull());
        radgull.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        radgull.setAttacking(true);
        Permanent unchosen = addCreatureReady(player1, new VexingRadgull());
        unchosen.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId(), radgull.getId()));

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(1);
        assertThat(radgull.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Radgulls give two rad counters then proliferate rather than giving four")
    void multipleTriggersCheckRadCountersAtResolution() {
        addCreatureReady(player1, new VexingRadgull()).setAttacking(true);
        addCreatureReady(player1, new VexingRadgull()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Radgull gives rad counters to the player it damages")
    void opposingControllerGivesRadCountersToDamagedPlayer() {
        addCreatureReady(player2, new VexingRadgull()).setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
