package com.github.laxika.magicalvibes.cards.v;

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
}
