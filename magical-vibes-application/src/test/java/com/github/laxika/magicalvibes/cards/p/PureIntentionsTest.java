package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PureIntentionsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns cards discarded because of an opponent's spell")
    void returnsCardsDiscardedByOpponent() {
        harness.setHand(player1, List.of(new PureIntentions(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Distress()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return cards discarded by its own controller")
    void doesNotReturnCardsDiscardedByController() {
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new PureIntentions()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Sift(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.setHand(player2, List.of(new Distress()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Forest");
    }

    @Test
    @DisplayName("Returns itself at the beginning of the next end step when discarded by an opponent")
    void returnsItselfAtNextEndStepWhenDiscardedByOpponent() {
        harness.setHand(player2, List.of(new PureIntentions()));
        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Pure Intentions");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        harness.assertNotInGraveyard(player2, "Pure Intentions");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Pure Intentions");
    }

    @Test
    @DisplayName("Does not return itself when discarded by its own controller")
    void doesNotReturnItselfWhenDiscardedByController() {
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Sift(), new PureIntentions()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Pure Intentions");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        harness.assertInGraveyard(player1, "Pure Intentions");
    }
}
