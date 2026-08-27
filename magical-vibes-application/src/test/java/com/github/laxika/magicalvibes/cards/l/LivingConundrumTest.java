package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({LivingConundrum.class, GrizzlyBears.class})
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
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
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

    private void drawForPlayer1() {
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
