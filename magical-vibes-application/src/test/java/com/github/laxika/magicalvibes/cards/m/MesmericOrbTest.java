package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MesmericOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Each untapped permanent makes its controller mill a card")
    void eachUntappedPermanentMillsItsController() {
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent player1Permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        player1Permanent.tap();
        trimDeck(player1, 10);

        runUntapStep(player1);
        resolveStack();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's untapped permanent makes that opponent mill a card")
    void opponentsUntappedPermanentMillsOpponent() {
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent player2Permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        player2Permanent.tap();
        trimDeck(player2, 10);

        runUntapStep(player2);
        resolveStack();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The controller is remembered if the untapped permanent leaves before resolution")
    void remembersControllerIfPermanentLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new MesmericOrb());
        Permanent player2Permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        player2Permanent.tap();
        trimDeck(player2, 10);

        harness.inMutationScope(() -> {
            player2Permanent.untap();
            harness.getTriggerCollectionService().checkBecomesUntappedTriggers(gd, player2Permanent);
        });
        gd.playerBattlefields.get(player2.getId()).remove(player2Permanent);
        resolveStack();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    private void trimDeck(Player player, int size) {
        while (gd.playerDecks.get(player.getId()).size() > size) {
            gd.playerDecks.get(player.getId()).removeFirst();
        }
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

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }
}
