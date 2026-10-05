package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AlmsCollector;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OverflowingInsight.class, JungleDelver.class, AlmsCollector.class})
class OverflowingInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws seven cards")
    void targetPlayerDrawsSevenCards() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castOverflowingInsightTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 7);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        castOverflowingInsightTargeting(player1.getId());
        harness.passBothPriorities();

        // setHand sets hand to [OverflowingInsight], casting removes it (0), then draws 7
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Does not affect non-targeted player")
    void doesNotAffectNonTargetedPlayer() {
        castOverflowingInsightTargeting(player2.getId());
        harness.passBothPriorities();

        // Player 1's hand should only lose the card that was cast (setHand sets 1 card, cast removes it)
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        castOverflowingInsightTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Overflowing Insight");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new JungleDelver());

        harness.setHand(player1, List.of(new OverflowingInsight()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Drawing exactly seven remaining cards does not lose the game")
    void exactlySevenRemainingCardsDoesNotLose() {
        var library = IntStream.range(0, 7).mapToObj(i -> new JungleDelver()).toList();
        harness.setLibrary(player2, library);
        harness.setHand(player2, List.of());

        castOverflowingInsightTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Target loses when fewer than seven cards remain in the library")
    void fewerThanSevenRemainingCardsLoses() {
        var library = IntStream.range(0, 6).mapToObj(i -> new JungleDelver()).toList();
        harness.setLibrary(player2, library);
        harness.setHand(player2, List.of());

        castOverflowingInsightTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Alms Collector replaces the seven-card draw with one card for each player")
    void almsCollectorReplacesSevenCardDraw() {
        harness.addToBattlefield(player1, new AlmsCollector());
        harness.setHand(player2, List.of());

        castOverflowingInsightTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    private void castOverflowingInsightTargeting(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new OverflowingInsight()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castSorcery(player1, 0, targetPlayerId);
    }
}
