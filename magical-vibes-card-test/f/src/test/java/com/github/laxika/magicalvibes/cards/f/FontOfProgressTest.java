package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FontOfProgress.class})
class FontOfProgressTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two oil counters")
    void entersWithTwoOilCounters() {
        harness.setHand(player1, List.of(new FontOfProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent font = findPermanent(player1, "Font of Progress");
        assertThat(font.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Target player mills the current number of oil counters")
    void millsCurrentOilCounterCount() {
        Permanent font = addReadyFont(player1);
        font.setCounterCount(CounterType.OIL, 3);
        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The mill ability can target its controller")
    void canTargetController() {
        Permanent font = addReadyFont(player1);
        font.setCounterCount(CounterType.OIL, 1);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("The mill ability requires the Font to be untapped")
    void cannotActivateWhileTapped() {
        Permanent font = addReadyFont(player1);
        font.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent addReadyFont(Player player) {
        Permanent font = harness.addToBattlefieldAndReturn(player, new FontOfProgress());
        font.setSummoningSick(false);
        return font;
    }

    @Test
    @DisplayName("Oil counters are counted at resolution rather than activation")
    void countsOilCountersAtResolution() {
        Permanent font = addReadyFont(player1);
        font.setCounterCount(CounterType.OIL, 2);
        harness.setLibrary(player2, List.of(new FontOfProgress(), new FontOfProgress(),
                new FontOfProgress(), new FontOfProgress()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(font.isTapped()).isTrue();
        font.setCounterCount(CounterType.OIL, 3);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(font.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    @DisplayName("Zero oil counters mills no cards")
    void zeroOilCountersMillsNothing() {
        addReadyFont(player1);
        harness.setLibrary(player2, List.of(new FontOfProgress()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milling more cards than remain mills the entire library")
    void millsOnlyAvailableCards() {
        Permanent font = addReadyFont(player1);
        font.setCounterCount(CounterType.OIL, 3);
        harness.setLibrary(player2, List.of(new FontOfProgress()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A departed Font uses its oil counter count just before leaving")
    void usesLastKnownOilCounters() {
        Permanent font = addReadyFont(player1);
        font.setCounterCount(CounterType.OIL, 2);
        harness.setLibrary(player2, List.of(new FontOfProgress(), new FontOfProgress(),
                new FontOfProgress(), new FontOfProgress()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        font.setCounterCount(CounterType.OIL, 3);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, font);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Font of Progress");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A noncreature Font can tap the turn it enters")
    void canActivateImmediatelyAfterEntering() {
        harness.setHand(player1, List.of(new FontOfProgress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.setLibrary(player2, List.of(new FontOfProgress(), new FontOfProgress(),
                new FontOfProgress()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }
}
