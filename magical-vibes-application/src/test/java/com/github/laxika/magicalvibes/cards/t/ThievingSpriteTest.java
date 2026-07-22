package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SpellstutterSprite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThievingSpriteTest extends BaseCardTest {

    private PendingInteraction.RevealCardsDiscardChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
    }

    private void castThievingSprite(java.util.UUID targetPlayerId) {
        harness.setHand(player1, new ArrayList<>(List.of(new ThievingSprite())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, 0, targetPlayerId);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger
    }

    // ===== X = 1 (only Thieving Sprite is a Faerie), hand larger than X =====

    @Test
    @DisplayName("Target reveals one card of their choice, then controller picks it to discard")
    void revealsOneThenControllerDiscards() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant())));

        castThievingSprite(player2.getId());

        // Phase 1: target chooses which single card to reveal (X = 1 Faerie — counts itself).
        PendingInteraction.RevealCardsDiscardChoice reveal = activeChoice();
        assertThat(reveal).isNotNull();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(reveal.remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player2, 1); // reveal Hill Giant

        // Phase 2: controller chooses the revealed card for the target to discard.
        PendingInteraction.RevealCardsDiscardChoice discard = activeChoice();
        assertThat(discard).isNotNull();
        assertThat(discard.revealStage()).isFalse();
        assertThat(discard.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(discard.revealedCardIds()).hasSize(1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .matches(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Hand already <= X: whole hand revealed, no phase-1 choice =====

    @Test
    @DisplayName("When the hand is not larger than X, every card is revealed and controller picks directly")
    void wholeHandRevealedWhenSmall() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        castThievingSprite(player2.getId());

        // Only one card and X = 1 -> skip phase 1, go straight to the discard pick.
        PendingInteraction.RevealCardsDiscardChoice discard = activeChoice();
        assertThat(discard).isNotNull();
        assertThat(discard.revealStage()).isFalse();
        assertThat(discard.decidingPlayerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== X scales with the number of Faeries controlled =====

    @Test
    @DisplayName("With two Faeries, the target reveals two cards and only the chosen one is discarded")
    void twoFaeriesRevealTwo() {
        harness.addToBattlefield(player1, new SpellstutterSprite()); // second Faerie -> X = 2
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant(), new GrizzlyBears())));

        castThievingSprite(player2.getId());

        PendingInteraction.RevealCardsDiscardChoice reveal = activeChoice();
        assertThat(reveal).isNotNull();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0); // reveal first card
        // Still revealing the second card.
        assertThat(activeChoice().remainingCount()).isEqualTo(1);
        harness.handleCardChosen(player2, 1); // reveal Hill Giant

        PendingInteraction.RevealCardsDiscardChoice discard = activeChoice();
        assertThat(discard.revealStage()).isFalse();
        assertThat(discard.revealedCardIds()).hasSize(2);

        harness.handleCardChosen(player1, 1); // discard the second revealed card (Hill Giant)

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
        // Two cards remain: the other revealed card plus the never-revealed one.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    // ===== Empty hand =====

    @Test
    @DisplayName("Target with an empty hand reveals nothing and no choice is prompted")
    void emptyHandDoesNothing() {
        harness.setHand(player2, new ArrayList<>());

        castThievingSprite(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("empty"));
    }

    // ===== Rejections =====

    @Test
    @DisplayName("The target cannot make the controller's discard pick")
    void wrongPlayerCannotDiscardChoose() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        castThievingSprite(player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("An invalid reveal index is rejected")
    void invalidRevealIndexRejected() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant())));

        castThievingSprite(player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }
}
