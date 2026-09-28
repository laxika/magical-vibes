package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WellRested.class, GrizzlyBears.class})
class WellRestedTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping the enchanted creature puts two counters on it, gains life, and draws a card")
    void untappingEnchantedCreatureResolvesAllEffects() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, creature);
        creature.tap();
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.performUntapStep(player1);
        resolveStack();

        assertThat(creature.getCounters().get(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Well Rested triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, creature);
        creature.tap();
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.performUntapStep(player1);
        resolveStack();

        creature.tap();
        harness.performUntapStep(player1);
        resolveStack();

        assertThat(creature.getCounters().get(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("The Aura also triggers for an opponent's enchanted creature")
    void triggersForOpponentControlledEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player1, creature);
        creature.tap();
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.performUntapStep(player2);
        resolveStack();

        assertThat(creature.getCounters().get(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private Permanent attachAura(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new WellRested());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }
}
