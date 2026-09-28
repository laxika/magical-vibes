package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MortarionDaemonPrimarch.class)
class MortarionDaemonPrimarchTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, X is capped at life lost this turn and creates black Astartes Warrior tokens")
    void paysUpToLifeLostAndCreatesTokens() {
        harness.addToBattlefield(player1, new MortarionDaemonPrimarch());
        gd.lifeLostThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);

        List<Permanent> tokens = findPermanents(player1, "Astartes Warrior");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes())
                    .contains(CardSubtype.ASTARTES, CardSubtype.WARRIOR);
            assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
        });
    }

    @Test
    @DisplayName("Does not prompt when the controller has lost no life this turn")
    void noLifeLostDoesNotPrompt() {
        harness.addToBattlefield(player1, new MortarionDaemonPrimarch());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Astartes Warrior")).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
