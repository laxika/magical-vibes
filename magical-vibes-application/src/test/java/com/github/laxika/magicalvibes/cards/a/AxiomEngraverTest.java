package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AxiomEngraver.class, Forest.class})
class AxiomEngraverTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two oil counters")
    void entersWithTwoOilCounters() {
        harness.castFromHand(player1, new AxiomEngraver(), "{1}{R}");
        harness.passBothPriorities();

        Permanent engraver = findPermanent(player1, "Axiom Engraver");
        assertThat(engraver.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes an oil counter, discards a card, and draws a card")
    void removesCounterDiscardsAndDraws() {
        Permanent engraver = addReadyEngraver(player1, 2);
        harness.setHand(player1, List.of(new AxiomEngraver()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(engraver.isTapped()).isTrue();
        assertThat(engraver.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Axiom Engraver");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("Cannot activate without an oil counter or a card to discard")
    void cannotActivateWithoutRequiredCosts() {
        Permanent engraver = addReadyEngraver(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");

        engraver.setCounterCount(CounterType.OIL, 1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enters with oil counters when put directly onto the battlefield")
    void entersWithCountersWithoutBeingCast() {
        Permanent engraver = harness.enterBattlefieldAndReturn(player1, new AxiomEngraver());

        assertThat(engraver.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while tapped or summoning sick")
    void cannotActivateWithoutBeingAbleToTap() {
        Permanent engraver = addReadyEngraver(player1, 2);
        harness.setHand(player1, List.of(new Forest()));
        engraver.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        engraver.setSummoningSick(false);
        engraver.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(engraver.getCounterCount(CounterType.OIL)).isEqualTo(2);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Can spend the last oil counter and discard a land")
    void canSpendLastCounterAndDiscardLand() {
        Permanent engraver = addReadyEngraver(player1, 1);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new AxiomEngraver()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(engraver.getCounterCount(CounterType.OIL)).isZero();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Axiom Engraver");

        engraver.setTapped(false);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    @DisplayName("The activated ability still draws if its source leaves the battlefield")
    void drawsAfterSourceLeavesBattlefield() {
        Permanent engraver = addReadyEngraver(player1, 2);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new AxiomEngraver()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(engraver);
        gd.playerGraveyards.get(player1.getId()).add(engraver.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Axiom Engraver");
    }

    private Permanent addReadyEngraver(Player player, int oilCounters) {
        Permanent engraver = addCreatureReady(player, new AxiomEngraver());
        engraver.setCounterCount(CounterType.OIL, oilCounters);
        return engraver;
    }
}
