package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegalBloodlordTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Creates a 1/1 black flying Bat token at end step if you gained life this turn")
    void createsBatTokenWhenLifeGained() {
        harness.addToBattlefield(player1, new RegalBloodlord());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        var bats = findPermanents(player1, "Bat");
        assertThat(bats).hasSize(1);
        assertThat(bats).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Creates no token at end step if you did not gain life this turn")
    void noTokenWithoutLifeGain() {
        harness.addToBattlefield(player1, new RegalBloodlord());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bat")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers on an opponent's end step when you gained life")
    void triggersOnOpponentEndStep() {
        harness.addToBattlefield(player1, new RegalBloodlord());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when only an opponent gained life")
    void opponentLifeGainDoesNotTrigger() {
        harness.addToBattlefield(player1, new RegalBloodlord());
        gd.lifeGainedThisTurn.put(player2.getId(), 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bat")).isEmpty();
    }
}
