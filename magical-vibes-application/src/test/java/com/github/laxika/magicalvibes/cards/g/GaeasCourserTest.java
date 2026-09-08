package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GaeasCourserTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with three creature cards in your graveyard draws a card")
    void attackingWithThreeCreatureCardsDraws() {
        addCreatureReady(player1, new GaeasCourser());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, List.of(0), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Noncreature cards and cards in an opponent's graveyard do not count")
    void onlyOwnCreatureCardsCount() {
        addCreatureReady(player1, new GaeasCourser());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, List.of(0), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The graveyard condition is checked again when the trigger resolves")
    void conditionMustStillBeMetOnResolution() {
        addCreatureReady(player1, new GaeasCourser());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        declareAttackers(player1, List.of(0), null);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
