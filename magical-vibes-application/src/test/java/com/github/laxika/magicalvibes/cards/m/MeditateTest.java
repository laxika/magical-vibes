package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Meditate.class, Forest.class})
class MeditateTest extends BaseCardTest {

    private List<Card> castMeditate() {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            library.add(new Forest());
        }
        harness.setLibrary(player1, library);

        harness.castFromHand(player1, new Meditate(), "{2}{U}");
        harness.passBothPriorities();
        return library;
    }

    @Test
    @DisplayName("Draws four cards")
    void drawsFourCards() {
        List<Card> library = castMeditate();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2), library.get(3));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(4), library.get(5));
    }

    @Test
    @DisplayName("Queues a skip of the caster's next turn only")
    void queuesSkipNextTurn() {
        castMeditate();

        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextTurnCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Skipping a turn leaves the untap, draw and combat-phase queues alone")
    void queuesNothingButTheTurnSkip() {
        castMeditate();

        assertThat(gd.skipNextUntapStepCount).isEmpty();
        assertThat(gd.skipNextDrawStepCount).isEmpty();
        assertThat(gd.skipNextCombatPhaseCount).isEmpty();
    }

    @Test
    @DisplayName("The caster's next turn is skipped")
    void skipsNextTurn() {
        castMeditate();

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);

        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player2, TurnStep.UNTAP);
    }
}
