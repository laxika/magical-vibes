package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JourneyersKite;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImiStatue.class, JourneyersKite.class, Forest.class})
class ImiStatueTest extends BaseCardTest {

    @Test
    @DisplayName("Only the chosen artifact untaps; other artifacts stay tapped, non-artifacts untap")
    void picksOneArtifactToUntap() {
        addCreatureReady(player1, new ImiStatue());
        Permanent firstKite = addCreatureReady(player1, new JourneyersKite());
        Permanent secondKite = addCreatureReady(player1, new JourneyersKite());
        Permanent firstForest = addCreatureReady(player1, new Forest());
        Permanent secondForest = addCreatureReady(player1, new Forest());
        firstKite.tap();
        secondKite.tap();
        firstForest.tap();
        secondForest.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstKite.getId()));

        assertThat(firstKite.isTapped()).isFalse();
        assertThat(secondKite.isTapped()).isTrue();
        assertThat(firstForest.isTapped()).isFalse();
        assertThat(secondForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Imi Statue still imposes the restriction")
    void tappedStatueStillRestricts() {
        Permanent statue = addCreatureReady(player1, new ImiStatue());
        statue.tap();
        Permanent kite = addCreatureReady(player1, new JourneyersKite());
        kite.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(kite.getId()));

        assertThat(kite.isTapped()).isFalse();
        assertThat(statue.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A single tapped artifact untaps without a choice")
    void singleArtifactUntapsNormally() {
        addCreatureReady(player1, new ImiStatue());
        Permanent kite = addCreatureReady(player1, new JourneyersKite());
        Permanent forest = addCreatureReady(player1, new Forest());
        kite.tap();
        forest.tap();

        advanceToNextTurn(player2);

        assertThat(kite.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Imi Statue restricts your untap step too")
    void opponentStatueRestrictsYourUntap() {
        addCreatureReady(player2, new ImiStatue());
        Permanent kite = addCreatureReady(player1, new JourneyersKite());
        Permanent otherKite = addCreatureReady(player1, new JourneyersKite());
        Permanent forest = addCreatureReady(player1, new Forest());
        kite.tap();
        otherKite.tap();
        forest.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(kite.getId()));

        assertThat(kite.isTapped()).isFalse();
        assertThat(otherKite.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
