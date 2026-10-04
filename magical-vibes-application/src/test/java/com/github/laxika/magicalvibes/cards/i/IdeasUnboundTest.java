package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.g.GhostLitRedeemer;
import com.github.laxika.magicalvibes.cards.k.KitsuneBonesetter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IdeasUnbound.class, ArabaMothrider.class, GhostLitRedeemer.class, KitsuneBonesetter.class})
class IdeasUnboundTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards immediately and discards three at the next end step")
    void drawsThenDiscardsAtNextEndStep() {
        harness.setHand(player1, new ArrayList<>(List.of(
                new IdeasUnbound(), new ArabaMothrider(), new GhostLitRedeemer(), new KitsuneBonesetter())));
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new ArabaMothrider(), new GhostLitRedeemer(), new KitsuneBonesetter())));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Ideas Unbound");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Discards all remaining cards when fewer than three remain")
    void discardsRemainingCardsAfterChannelingDrawnCard() {
        harness.setHand(player1, List.of(new IdeasUnbound()));
        harness.setLibrary(player1, List.of(
                new ArabaMothrider(), new GhostLitRedeemer(), new KitsuneBonesetter()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateHandAbility(player1, 1, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty hand does not leave a discard interaction pending")
    void emptyHandAtEndStep() {
        harness.setHand(player1, List.of(new IdeasUnbound()));
        harness.setLibrary(player1, List.of(
                new GhostLitRedeemer(), new GhostLitRedeemer(), new GhostLitRedeemer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.addMana(player1, ManaColor.WHITE, 3);
        for (int i = 0; i < 3; i++) {
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
        }
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two resolutions create independent three-card discard triggers")
    void multipleResolutionsDiscardSixCards() {
        harness.setHand(player1, List.of(new IdeasUnbound(), new IdeasUnbound()));
        harness.setLibrary(player1, List.of(
                new ArabaMothrider(), new GhostLitRedeemer(), new KitsuneBonesetter(),
                new ArabaMothrider(), new GhostLitRedeemer(), new KitsuneBonesetter()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.passBothPriorities();
        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(8);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The next end step can belong to the opponent and the trigger fires only once")
    void discardsControllerCardsAtOpponentsEndStepOnlyOnce() {
        harness.setHand(player1, List.of(new IdeasUnbound(), new KitsuneBonesetter()));
        harness.setHand(player2, List.of(new ArabaMothrider()));
        harness.setLibrary(player1, List.of(
                new ArabaMothrider(), new GhostLitRedeemer(), new KitsuneBonesetter()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
