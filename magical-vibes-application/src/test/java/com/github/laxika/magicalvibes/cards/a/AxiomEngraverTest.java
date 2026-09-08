package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AxiomEngraverTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two oil counters")
    void entersWithTwoOilCounters() {
        harness.setHand(player1, List.of(new AxiomEngraver()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent engraver = findEngraver(player1);
        assertThat(engraver.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes an oil counter, discards a card, and draws a card")
    void removesCounterDiscardsAndDraws() {
        Permanent engraver = addReadyEngraver(player1, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(engraver.isTapped()).isTrue();
        assertThat(engraver.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without an oil counter or a card to discard")
    void cannotActivateWithoutRequiredCosts() {
        Permanent engraver = addReadyEngraver(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");

        engraver.setCounterCount(CounterType.OIL, 1);
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyEngraver(Player player, int oilCounters) {
        Permanent engraver = new Permanent(new AxiomEngraver());
        engraver.setSummoningSick(false);
        engraver.setCounterCount(CounterType.OIL, oilCounters);
        gd.playerBattlefields.get(player.getId()).add(engraver);
        return engraver;
    }

    private Permanent findEngraver(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AxiomEngraver)
                .findFirst()
                .orElseThrow();
    }
}
