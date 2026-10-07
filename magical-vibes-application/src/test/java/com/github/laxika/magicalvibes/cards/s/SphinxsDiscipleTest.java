package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphinxsDisciple.class})
class SphinxsDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping Sphinx's Disciple makes its controller draw a card")
    void untappingDrawsACard() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new SphinxsDisciple());
        disciple.tap();
        harness.setLibrary(player1, List.of(new SphinxsDisciple(), new SphinxsDisciple()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        runUntapStep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Sphinx's Disciple does not trigger while it remains untapped")
    void remainsUntappedDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new SphinxsDisciple());
        harness.setLibrary(player1, List.of(new SphinxsDisciple(), new SphinxsDisciple()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        runUntapStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    private void runUntapStep(Player untappingPlayer) {
        Player opponent = untappingPlayer.equals(player1) ? player2 : player1;
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    void drawsBeforeDrawStep() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new SphinxsDisciple());
        disciple.tap();
        harness.setLibrary(player1, List.of(new SphinxsDisciple(), new SphinxsDisciple()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(disciple.isTapped()).isFalse();
        assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    void multipleDisciplesDrawForTheirController() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SphinxsDisciple());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SphinxsDisciple());
        first.tap();
        second.tap();
        harness.setLibrary(player2, List.of(new SphinxsDisciple(), new SphinxsDisciple(), new SphinxsDisciple()));
        int controllerHandSizeBefore = gd.playerHands.get(player2.getId()).size();
        int opponentHandSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandSizeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandSizeBefore);
    }

    @Test
    void opponentsUntapDoesNotTrigger() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new SphinxsDisciple());
        disciple.tap();
        harness.setLibrary(player1, List.of(new SphinxsDisciple(), new SphinxsDisciple()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(disciple.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }
}
