package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({WordsOfWind.class, Island.class, WordsOfWorship.class, WarpedDevotion.class})
class WordsOfWindTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses a permanent before the selected permanents return")
    void eachPlayerChoosesPermanentBeforeReturningThem() {
        Permanent wordsOfWind = harness.addToBattlefieldAndReturn(player1, new WordsOfWind());
        Permanent player1FirstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent player1SecondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent player2FirstIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent player2SecondIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of());
        Island drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));

        activateWordsOfWind();
        draw(player1);

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactly(
                wordsOfWind.getId(), player1FirstIsland.getId(), player1SecondIsland.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(player1SecondIsland.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactly(player2FirstIsland.getId(), player2SecondIsland.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(wordsOfWind, player1FirstIsland, player1SecondIsland);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(player2FirstIsland, player2SecondIsland);

        harness.handleMultiplePermanentsChosen(player2, List.of(player2FirstIsland.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(wordsOfWind, player1FirstIsland);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(player2SecondIsland);
        assertThat(gd.playerHands.get(player1.getId())).contains(player1SecondIsland.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(player2FirstIsland.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        draw(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1SecondIsland.getCard(), drawnCard);
    }

    @Test
    @DisplayName("A replacement with no choice still replaces the draw")
    void noChoiceStillReplacesDraw() {
        Permanent wordsOfWind = harness.addToBattlefieldAndReturn(player1, new WordsOfWind());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));

        activateWordsOfWind();
        draw(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wordsOfWind.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the activating player's draw is replaced")
    void onlyActivatingPlayersDrawIsReplaced() {
        Permanent wordsOfWind = harness.addToBattlefieldAndReturn(player1, new WordsOfWind());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Island player1Draw = new Island();
        Island player2Draw = new Island();
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Draw));

        activateWordsOfWind();
        draw(player2);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Draw);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Draw);

        draw(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wordsOfWind.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1Draw);
    }

    @Test
    @DisplayName("Repeated activations replace successive draws")
    void repeatedActivationsReplaceSuccessiveDraws() {
        Permanent wordsOfWind = harness.addToBattlefieldAndReturn(player1, new WordsOfWind());
        harness.setHand(player1, List.of());
        Island firstDraw = new Island();
        harness.setLibrary(player1, List.of(firstDraw, new Island(), new Island()));

        activateWordsOfWind(2);
        draw(player1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wordsOfWind.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        draw(player1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wordsOfWind.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        draw(player1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wordsOfWind.getCard(), firstDraw);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The delayed replacement expires at cleanup")
    void replacementExpiresAtCleanup() {
        Permanent wordsOfWind = harness.addToBattlefieldAndReturn(player1, new WordsOfWind());
        harness.setHand(player1, List.of());
        Island drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));

        activateWordsOfWind();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        draw(player1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(wordsOfWind);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A returned permanent observes all simultaneous returns")
    void returnedPermanentObservesSimultaneousReturns() {
        Permanent wordsOfWind = harness.addToBattlefieldAndReturn(player1, new WordsOfWind());
        Permanent devotion = harness.addToBattlefieldAndReturn(player1, new WarpedDevotion());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Island()));
        activateWordsOfWind();

        draw(player1);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.handleMultiplePermanentsChosen(player1, List.of(devotion.getId())));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(wordsOfWind);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(devotion.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(island.getCard());
        assertThat(gd.stack).filteredOn(entry -> entry.getCard() instanceof WarpedDevotion).hasSize(2);
    }

    @Test
    @DisplayName("The drawing player chooses between competing draw replacements")
    void drawingPlayerChoosesBetweenCompetingReplacements() {
        Permanent wordsOfWind = harness.addToBattlefieldAndReturn(player1, new WordsOfWind());
        Permanent wordsOfWorship = harness.addToBattlefieldAndReturn(player1, new WordsOfWorship());
        harness.setHand(player1, List.of());
        Island drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);

        activateWordsOfWind();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        draw(player1);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(wordsOfWind, wordsOfWorship);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("The active player chooses first even when the nonactive player replaces a draw")
    void activePlayerChoosesFirstDuringOpponentsTurn() {
        Permanent wordsOfWind = harness.addToBattlefieldAndReturn(player1, new WordsOfWind());
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent thirdIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Island()));
        activateWordsOfWind();
        harness.forceActivePlayer(player2);

        draw(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(secondIsland.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(secondIsland, thirdIsland);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstIsland.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(wordsOfWind);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(thirdIsland);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstIsland.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(secondIsland.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Replacing a draw from an empty library does not cause a failed draw")
    void replacesDrawFromEmptyLibrary() {
        Permanent wordsOfWind = harness.addToBattlefieldAndReturn(player1, new WordsOfWind());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        activateWordsOfWind();

        draw(player1);
        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(wordsOfWind.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Remaining cards in a multi-card draw wait for permanent choices")
    void multiCardDrawResumesAfterPermanentChoices() {
        Permanent wordsOfWind = harness.addToBattlefieldAndReturn(player1, new WordsOfWind());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of());
        Island firstDraw = new Island();
        Island secondDraw = new Island();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        activateWordsOfWind();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.handleMultiplePermanentsChosen(player1, List.of(island.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(wordsOfWind);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island.getCard(), firstDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondDraw);
    }

    private void activateWordsOfWind() {
        activateWordsOfWind(1);
    }

    private void activateWordsOfWind(int activations) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, activations);
        for (int i = 0; i < activations; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
