package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TidehollowScullerTest extends BaseCardTest {

    /**
     * Casts Tidehollow Sculler targeting player2, resolves it, and resolves the ETB trigger
     * so the hand-reveal prompt appears.
     */
    private void castAndResolveETB() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TidehollowSculler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> creature enters, ETB on stack
        harness.passBothPriorities(); // resolve ETB -> hand reveal + choice prompt
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    // ===== ETB exile =====

    @Test
    @DisplayName("ETB reveals opponent's hand in exile mode")
    void etbRevealsHandInExileMode() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));

        castAndResolveETB();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).exileMode()).isTrue();
    }

    @Test
    @DisplayName("Choosing a nonland card exiles it")
    void choosingNonlandExilesIt() {
        Card instant = new Peek();
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(instant, land)));

        castAndResolveETB();

        // Choose Peek (index 0)
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));
        // Land stays in hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Creature cards are valid choices; only lands are excluded")
    void creatureCardsAreValidLandsExcluded() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(creature, land, instant)));

        castAndResolveETB();

        // Creature (0) and instant (2) are valid; land (1) is excluded
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 2);
    }

    @Test
    @DisplayName("Hand with only lands results in no valid choices")
    void handWithOnlyLandsNoChoices() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        castAndResolveETB();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no valid choices"));
    }

    @Test
    @DisplayName("Empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());

        castAndResolveETB();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("empty"));
    }

    // ===== Return on source leave =====

    @Test
    @DisplayName("Exiled card returns to hand when Sculler dies")
    void exiledCardReturnsToHandWhenScullerDies() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));

        castAndResolveETB();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(c -> c.getName().equals("Peek"));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();

        resetForFollowUpSpell();

        // Kill Sculler with Lightning Bolt
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID scullerId = harness.getPermanentId(player1, "Tidehollow Sculler");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, scullerId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Tidehollow Sculler"));
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(c -> c.getName().equals("Peek"));
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(c -> c.getName().equals("Peek"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Exiled card returns to hand when Sculler is bounced")
    void exiledCardReturnsToHandWhenScullerBounced() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));

        castAndResolveETB();
        harness.handleCardChosen(player1, 0);

        resetForFollowUpSpell();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID scullerId = harness.getPermanentId(player1, "Tidehollow Sculler");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, scullerId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).anyMatch(c -> c.getName().equals("Peek"));
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(c -> c.getName().equals("Peek"));
    }
}
