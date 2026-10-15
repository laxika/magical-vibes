package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingConundrum.class})
class LivingConundrumTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 10/10 with flying and vigilance while its controller's library is empty")
    void emptyLibraryGrantsPowerToughnessAndKeywords() {
        Permanent conundrum = addCreatureReady(player1, new LivingConundrum());
        harness.setLibrary(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, conundrum)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, conundrum)).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, conundrum, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, conundrum, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Skips an empty-library draw without losing the game")
    void skipsEmptyLibraryDraw() {
        Permanent conundrum = addCreatureReady(player1, new LivingConundrum());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());

        drawForPlayer1();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, conundrum)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, conundrum)).isEqualTo(10);
    }

    @Test
    @DisplayName("Draws normally while the library has cards")
    void drawsNormallyWithCardsInLibrary() {
        Permanent conundrum = addCreatureReady(player1, new LivingConundrum());
        harness.setLibrary(player1, List.of(new LivingConundrum()));
        harness.setHand(player1, List.of());

        assertThat(gqs.getEffectivePower(gd, conundrum)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, conundrum)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, conundrum, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, conundrum, Keyword.VIGILANCE)).isFalse();

        drawForPlayer1();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, conundrum)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, conundrum)).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, conundrum, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, conundrum, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Library refills remove the empty-library bonus immediately")
    void refillingLibraryRemovesBonus() {
        Permanent conundrum = addCreatureReady(player1, new LivingConundrum());
        harness.setLibrary(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, conundrum)).isEqualTo(10);

        harness.setLibrary(player1, List.of(new LivingConundrum()));

        assertThat(gqs.getEffectivePower(gd, conundrum)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, conundrum)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, conundrum, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, conundrum, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Drawing multiple cards skips each draw after the last library card")
    void multiCardDrawSkipsRemainingDraws() {
        addCreatureReady(player1, new LivingConundrum());
        harness.setLibrary(player1, List.of(new LivingConundrum()));
        harness.setHand(player1, List.of());

        harness.getDrawService().resolveDrawCards(gd, player1.getId(), 3);

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not protect an opponent drawing from an empty library")
    void opponentStillLosesToEmptyLibraryDraw() {
        Permanent conundrum = addCreatureReady(player1, new LivingConundrum());
        harness.setLibrary(player1, List.of(new LivingConundrum()));
        harness.setLibrary(player2, List.of());

        assertThat(gqs.getEffectivePower(gd, conundrum)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, conundrum, Keyword.FLYING)).isFalse();
        harness.getDrawService().resolveDrawCard(gd, player2.getId());

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    private void drawForPlayer1() {
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
