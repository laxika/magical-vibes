package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AngelicAccordTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Creates a 4/4 white flying Angel token when you gained 4 life this turn")
    void createsAngelTokenOnFourLifeGained() {
        harness.addToBattlefield(player1, new AngelicAccord());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        advanceToEndStep(player1);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        var angels = findPermanents(player1, "Angel");
        assertThat(angels).hasSize(1);
        assertThat(angels).allSatisfy(t -> {
            assertThat(t.getCard().getPower()).isEqualTo(4);
            assertThat(t.getCard().getToughness()).isEqualTo(4);
            assertThat(t.getCard().isToken()).isTrue();
            assertThat(gqs.hasKeyword(gd, t, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Creates a token when you gained more than 4 life this turn")
    void createsAngelTokenOnMoreThanFourLifeGained() {
        harness.addToBattlefield(player1, new AngelicAccord());
        gd.lifeGainedThisTurn.put(player1.getId(), 7);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates no token when you gained fewer than 4 life this turn")
    void noTokenBelowThreshold() {
        harness.addToBattlefield(player1, new AngelicAccord());
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creates no token when you gained no life this turn")
    void noTokenWithoutLifeGain() {
        harness.addToBattlefield(player1, new AngelicAccord());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers on each end step, including the opponent's")
    void triggersOnOpponentEndStep() {
        harness.addToBattlefield(player1, new AngelicAccord());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);
    }

    @Test
    @DisplayName("Life gained by the opponent does not trigger your Angelic Accord")
    void opponentLifeGainDoesNotTrigger() {
        harness.addToBattlefield(player1, new AngelicAccord());
        gd.lifeGainedThisTurn.put(player2.getId(), 8);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isZero();
    }
}
