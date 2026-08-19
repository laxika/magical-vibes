package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlistenerSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three oil counters")
    void entersWithThreeOilCounters() {
        harness.setHand(player1, List.of(new GlistenerSeer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent seer = findSeer(player1);
        assertThat(seer.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing an oil counter lets Glistener Seer scry 1")
    void removesOilCounterAndScries() {
        Permanent seer = addReadySeer(player1, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(seer.getCounterCount(CounterType.OIL)).isEqualTo(0);
        assertThat(seer.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without an oil counter")
    void cannotActivateWithoutOilCounter() {
        addReadySeer(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    private Permanent addReadySeer(Player player, int counters) {
        Permanent seer = new Permanent(new GlistenerSeer());
        seer.setSummoningSick(false);
        seer.setCounterCount(CounterType.OIL, counters);
        gd.playerBattlefields.get(player.getId()).add(seer);
        return seer;
    }

    private Permanent findSeer(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GlistenerSeer)
                .findFirst()
                .orElseThrow();
    }
}
