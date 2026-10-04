package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlistenerSeer.class, Forest.class})
class GlistenerSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with three oil counters")
    void entersWithThreeOilCounters() {
        harness.setHand(player1, List.of(new GlistenerSeer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent seer = findPermanent(player1, "Glistener Seer");
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

    @Test
    void paysCostsBeforeScryResolves() {
        Permanent seer = addReadySeer(player1, 3);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(seer.isTapped()).isTrue();
        assertThat(seer.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canKeepTopCard() {
        addReadySeer(player1, 1);
        Forest top = new Forest();
        GlistenerSeer next = new GlistenerSeer();
        harness.setLibrary(player1, List.of(top, next));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canPutTopCardOnBottom() {
        addReadySeer(player1, 1);
        Forest top = new Forest();
        GlistenerSeer next = new GlistenerSeer();
        harness.setLibrary(player1, List.of(top, next));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesWithEmptyLibrary() {
        Permanent seer = addReadySeer(player1, 1);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(seer.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent seer = addReadySeer(player1, 3);
        seer.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(seer.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(seer.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent seer = addReadySeer(player1, 3);
        seer.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(seer.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    void otherCounterTypesCannotPayOilCost() {
        Permanent seer = addReadySeer(player1, 0);
        seer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        assertThat(seer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(seer.isTapped()).isFalse();
    }

    private Permanent addReadySeer(Player player, int counters) {
        Permanent seer = harness.addToBattlefieldAndReturn(player, new GlistenerSeer());
        seer.setSummoningSick(false);
        seer.setCounterCount(CounterType.OIL, counters);
        return seer;
    }
}
