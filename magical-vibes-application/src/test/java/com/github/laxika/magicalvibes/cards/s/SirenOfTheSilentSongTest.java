package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SirenOfTheSilentSongTest extends BaseCardTest {

    @Test
    void untappingMakesEachOpponentDiscardThenMill() {
        addTappedSiren();
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        runUntapStep();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    void emptyOpponentHandStillMills() {
        addTappedSiren();
        harness.setHand(player2, new ArrayList<>());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        runUntapStep();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    private Permanent addTappedSiren() {
        Permanent siren = harness.addToBattlefieldAndReturn(player1, new SirenOfTheSilentSong());
        siren.setSummoningSick(false);
        siren.tap();
        return siren;
    }

    private void runUntapStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
