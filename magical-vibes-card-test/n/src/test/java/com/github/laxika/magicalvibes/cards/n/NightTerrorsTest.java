package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightTerrors.class, WalkingCorpse.class, ThinkTwice.class, Forest.class})
class NightTerrorsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a player")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(NightTerrors.class);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Resolving reveals hand and prompts caster for card choice")
    void promptsForCardChoice() {
        Card card1 = new WalkingCorpse();
        Card card2 = new ThinkTwice();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).remainingCount()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).exileMode()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).discardMode()).isFalse();
    }

    @Test
    @DisplayName("Choosing a nonland card exiles it (not discards)")
    void choosingNonlandCardExilesIt() {
        Card card1 = new WalkingCorpse();
        Card card2 = new ThinkTwice();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Choose Walking Corpse (index 0)
        harness.handleCardChosen(player1, 0);

        // Choice is complete
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Walking Corpse should be in player2's exile zone, NOT graveyard
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
        harness.assertNotInGraveyard(player2, "Walking Corpse");

        // Think Twice should remain in player2's hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Think Twice");
    }

    @Test
    @DisplayName("Land cards are excluded from valid choices")
    void landCardsExcludedFromChoices() {
        Card creature = new WalkingCorpse();
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(creature, land)));

        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);

        // Only index 0 (Walking Corpse) should be valid, index 1 (Forest) is a land
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Selecting a land index is rejected")
    void selectingLandIndexIsRejected() {
        Card creature = new WalkingCorpse();
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(creature, land)));

        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Trying to choose the Forest (index 1, a land) should fail
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Hand with only lands results in no valid choices")
    void handWithOnlyLandsNoValidChoices() {
        Card land1 = new Forest();
        Card land2 = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(land1, land2)));

        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // No valid choices, so the effect should complete without prompting
        assertThat(gd.interaction.activeInteraction()).isNull();

        // Lands should remain in hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        // Log should indicate no valid choices
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no valid choices"));
    }

    @Test
    @DisplayName("Resolving against empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("empty"));
    }

    @Test
    @DisplayName("Hand with mix of lands and nonlands only allows nonland choice")
    void mixedHandOnlyAllowsNonlandChoice() {
        Card land1 = new Forest();
        Card creature = new WalkingCorpse();
        Card land2 = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(land1, creature, land2)));

        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        // Only index 1 (Walking Corpse) should be valid
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(1);

        // Choose the only nonland card
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));

        // Two forests remain in hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId()))
                .allMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Invalid card index is rejected")
    void invalidCardIndexRejected() {
        Card card1 = new WalkingCorpse();
        Card card2 = new ThinkTwice();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 5))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Wrong player cannot choose")
    void wrongPlayerCannotChoose() {
        Card card1 = new WalkingCorpse();
        Card card2 = new ThinkTwice();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("Can target self")
    void canTargetSelf() {
        Card card1 = new WalkingCorpse();
        Card card2 = new ThinkTwice();
        harness.setHand(player1, new ArrayList<>(List.of(new NightTerrors(), card1, card2)));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);

        // Choose Walking Corpse (index 0 after Night Terrors left hand)
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));

        // Think Twice should remain in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).get(0).getName()).isEqualTo("Think Twice");
    }

    @Test
    @DisplayName("Night Terrors goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card card1 = new WalkingCorpse();
        harness.setHand(player2, new ArrayList<>(List.of(card1)));

        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Night Terrors");
    }

    @Test
    @DisplayName("Hand reveal is logged")
    void handRevealIsLogged() {
        Card card1 = new WalkingCorpse();
        Card card2 = new ThinkTwice();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("reveals their hand"));
    }

    @Test
    @DisplayName("Card choice is logged")
    void cardChoiceIsLogged() {
        Card card1 = new WalkingCorpse();
        Card card2 = new ThinkTwice();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("chooses") && log.contains("Walking Corpse"));
    }

    @Test
    @DisplayName("Exile is logged")
    void exileIsLogged() {
        Card card1 = new WalkingCorpse();
        harness.setHand(player2, new ArrayList<>(List.of(card1)));

        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("exiles") && log.contains("Walking Corpse"));
    }

    @Test
    @DisplayName("Choosing one copy leaves other copies and unrelated cards in hand")
    void choosingOneCopyLeavesOtherCardsInHand() {
        Card firstCopy = new WalkingCorpse();
        Card chosenCopy = new WalkingCorpse();
        Card instant = new ThinkTwice();
        Card land = new Forest();
        harness.setHand(player2, List.of(firstCopy, chosenCopy, instant, land));
        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosenCopy);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstCopy, instant, land);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Night Terrors");
    }

    @Test
    @DisplayName("Caster must choose a nonland card and may choose an instant")
    void choiceIsMandatoryAndCanExileAnInstant() {
        Card instant = new ThinkTwice();
        Card land = new Forest();
        harness.setHand(player2, List.of(land, instant));
        harness.setHand(player1, List.of(new NightTerrors()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, instant);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(instant);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
