package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NagaOracleTest extends BaseCardTest {

    /** Casts Naga Oracle and resolves it plus its ETB trigger, leaving the surveil interaction active. */
    private GameData resolveEtbSurveil() {
        GameData gd = harness.getGameData();
        harness.setHand(player1, List.of(new NagaOracle()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
        return gd;
    }

    /** Replaces the top three library cards with three distinct, identity-trackable cards. */
    private Card[] seedTopThree(GameData gd) {
        Card top0 = new GrizzlyBears();
        Card top1 = new GrizzlyBears();
        Card top2 = new GrizzlyBears();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.add(0, top2);
        deck.add(0, top1);
        deck.add(0, top0);
        return new Card[]{top0, top1, top2};
    }

    @Test
    @DisplayName("ETB enters a surveil 3 interaction (reject pile is the graveyard)")
    void etbEntersSurveilState() {
        GameData gd = resolveEtbSurveil();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).hasSize(3);
        assertThat(surveil.toGraveyard()).isTrue();
    }

    @Test
    @DisplayName("Keeping all three on top preserves them in the library in order")
    void surveilKeepAllOnTop() {
        GameData gd = harness.getGameData();
        Card[] top = seedTopThree(gd);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        resolveEtbSurveil();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.get(0)).isSameAs(top[0]);
        assertThat(deck.get(1)).isSameAs(top[1]);
        assertThat(deck.get(2)).isSameAs(top[2]);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
    }

    @Test
    @DisplayName("Putting all three into the graveyard moves them off the library")
    void surveilAllToGraveyard() {
        GameData gd = harness.getGameData();
        Card[] top = seedTopThree(gd);

        resolveEtbSurveil();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1, 2)));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(top[0], top[1], top[2]);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(top[0], top[1], top[2]);
    }

    @Test
    @DisplayName("Splitting keeps chosen cards on top and bins the rest to the graveyard")
    void surveilSplitTopAndGraveyard() {
        GameData gd = harness.getGameData();
        Card[] top = seedTopThree(gd);

        resolveEtbSurveil();
        // Keep card 1 on top; bin cards 0 and 2.
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0, 2)));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(top[1]);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top[0], top[2]);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(top[1]);
    }

    @Test
    @DisplayName("Reordering the kept cards changes their library order")
    void surveilReorderOnTop() {
        GameData gd = harness.getGameData();
        Card[] top = seedTopThree(gd);

        resolveEtbSurveil();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 1, 0), List.of()));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.get(0)).isSameAs(top[2]);
        assertThat(deck.get(1)).isSameAs(top[1]);
        assertThat(deck.get(2)).isSameAs(top[0]);
    }

    @Test
    @DisplayName("Completing the surveil clears the interaction state")
    void surveilCompletionClearsState() {
        GameData gd = harness.getGameData();
        seedTopThree(gd);

        resolveEtbSurveil();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1, 2), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Empty library: surveil event occurs but there is nothing to look at")
    void emptyLibrarySurveil() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();

        resolveEtbSurveil();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("library is empty"));
    }
}
