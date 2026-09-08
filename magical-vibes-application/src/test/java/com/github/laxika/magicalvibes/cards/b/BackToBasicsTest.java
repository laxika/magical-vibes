package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SavageLands;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BackToBasicsTest extends BaseCardTest {

    @Test
    @DisplayName("Nonbasic lands stay tapped while basic lands untap")
    void nonbasicLandsStayTapped() {
        harness.addToBattlefield(player1, new BackToBasics());
        Permanent basicLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player1, new SavageLands());
        basicLand.tap();
        nonbasicLand.tap();

        advanceToNextTurn(player2);

        assertThat(basicLand.isTapped()).isFalse();
        assertThat(nonbasicLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The effect applies during an opponent's untap step")
    void affectsOpponentsNonbasicLands() {
        harness.addToBattlefield(player1, new BackToBasics());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new SavageLands());
        opponentLand.tap();

        advanceToNextTurn(player1);

        assertThat(opponentLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Nonbasic lands untap again after Back to Basics leaves the battlefield")
    void stopsApplyingAfterLeavingBattlefield() {
        Permanent backToBasics = harness.addToBattlefieldAndReturn(player1, new BackToBasics());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player1, new SavageLands());
        nonbasicLand.tap();
        gd.playerBattlefields.get(player1.getId()).remove(backToBasics);

        advanceToNextTurn(player2);

        assertThat(nonbasicLand.isTapped()).isFalse();
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
