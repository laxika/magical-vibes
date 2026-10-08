package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhisperAgent.class})
class WhisperAgentTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils one")
    void entersWithSurveilOne() {
        Card topCard = new WhisperAgent();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new WhisperAgent()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Surveil can leave the top card in place without disturbing the next card")
    void canKeepTopCard() {
        Card topCard = new WhisperAgent();
        Card nextCard = new WhisperAgent();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new WhisperAgent()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard, nextCard);
    }

    @Test
    @DisplayName("Entering with an empty library resolves without a choice or a loss")
    void emptyLibraryDoesNotRequireChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new WhisperAgent()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Whisper Agent");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's turn and surveils the caster's library")
    void canCastDuringOpponentsTurn() {
        Card ownTopCard = new WhisperAgent();
        Card opponentTopCard = new WhisperAgent();
        harness.setLibrary(player1, List.of(ownTopCard));
        harness.setLibrary(player2, List.of(opponentTopCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new WhisperAgent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Whisper Agent");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownTopCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentTopCard);
    }
}
