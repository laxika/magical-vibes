package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PraetorsCounselTest extends BaseCardTest {

    private void addPraetorsCounselMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 3);
    }

    // ===== Return all cards from graveyard to hand =====

    @Test
    @DisplayName("Returns all cards from graveyard to hand")
    void returnsAllCardsFromGraveyardToHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Put some cards in graveyard
        Card bears = new GrizzlyBears();
        Card mountain = new Mountain();
        Card forest = new Forest();
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(bears, mountain, forest));

        harness.setHand(player1, new ArrayList<>(List.of(new PraetorsCounsel())));
        addPraetorsCounselMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // All 3 graveyard cards should now be in hand
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).contains(bears, mountain, forest);
    }

    @Test
    @DisplayName("Works with empty graveyard")
    void worksWithEmptyGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, new ArrayList<>(List.of(new PraetorsCounsel())));
        addPraetorsCounselMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // No errors, graveyard still empty
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    // ===== Exile spell after resolution =====

    @Test
    @DisplayName("Praetor's Counsel is exiled after resolution, not put in graveyard")
    void exiledAfterResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        PraetorsCounsel counsel = new PraetorsCounsel();
        harness.setHand(player1, new ArrayList<>(List.of(counsel)));
        addPraetorsCounselMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Should be in exile, not graveyard
        assertThat(gd.playerExiledCards.get(player1.getId())).contains(counsel);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(counsel);
    }

    // ===== No maximum hand size for the rest of the game =====

    @Test
    @DisplayName("Grants no maximum hand size for the rest of the game")
    void grantsNoMaxHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, new ArrayList<>(List.of(new PraetorsCounsel())));
        addPraetorsCounselMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId());
    }

    @Test
    @DisplayName("Player does not have to discard during cleanup after Praetor's Counsel")
    void noDiscardDuringCleanup() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Put many cards in graveyard so hand exceeds 7 after resolution
        for (int i = 0; i < 10; i++) {
            gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        }

        harness.setHand(player1, new ArrayList<>(List.of(new PraetorsCounsel())));
        addPraetorsCounselMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Hand should have 10 cards (all returned from graveyard)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(10);

        // Advance to cleanup — no discard should be required
        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.DISCARD_CHOICE);
    }

    @Test
    @DisplayName("Opponent is not affected by Praetor's Counsel hand size grant")
    void opponentNotAffected() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, new ArrayList<>(List.of(new PraetorsCounsel())));
        addPraetorsCounselMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithNoMaximumHandSize).doesNotContain(player2.getId());

        // Verify opponent still has to discard with 9 cards
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new Forest(), new Forest(), new Forest(),
                new Mountain(), new Mountain(), new Mountain()
        )));

        gs.advanceStep(gd);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.awaitingCardChoicePlayerId()).isEqualTo(player2.getId());
    }

    // ===== Does not return opponent's graveyard =====

    @Test
    @DisplayName("Does not return cards from opponent's graveyard")
    void doesNotReturnOpponentGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card opponentCard = new GrizzlyBears();
        gd.playerGraveyards.get(player2.getId()).add(opponentCard);

        harness.setHand(player1, new ArrayList<>(List.of(new PraetorsCounsel())));
        addPraetorsCounselMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Opponent's graveyard should be untouched
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCard);
    }
}
