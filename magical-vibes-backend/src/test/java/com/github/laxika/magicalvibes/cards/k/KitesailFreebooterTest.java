package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToExileEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KitesailFreebooterTest extends BaseCardTest {

    /**
     * Casts Kitesail Freebooter targeting player2, resolves it, and resolves the ETB trigger
     * so the hand-reveal prompt appears.
     */
    private void castAndResolveETB() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new KitesailFreebooter()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> creature enters, ETB on stack
        harness.passBothPriorities(); // resolve ETB -> hand reveal + choice prompt
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Card has ChooseCardFromTargetHandToExileEffect with returnOnSourceLeave on ETB")
    void hasCorrectEffects() {
        KitesailFreebooter card = new KitesailFreebooter();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ChooseCardFromTargetHandToExileEffect.class);
        ChooseCardFromTargetHandToExileEffect effect =
                (ChooseCardFromTargetHandToExileEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.returnOnSourceLeave()).isTrue();
        assertThat(effect.excludedTypes()).containsExactlyInAnyOrder(CardType.CREATURE, CardType.LAND);
    }

    // ===== ETB exile =====

    @Test
    @DisplayName("ETB reveals opponent's hand and prompts for card choice")
    void etbRevealsHandAndPromptsChoice() {
        Card instant = new Peek();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(instant, creature)));

        castAndResolveETB();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.REVEALED_HAND_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.revealedHandChoice().exileMode()).isTrue();
    }

    @Test
    @DisplayName("Choosing a noncreature nonland card exiles it")
    void choosingNoncreatureNonlandExilesIt() {
        Card instant = new Peek();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(instant, creature)));

        castAndResolveETB();

        // Choose Peek (index 0) — an instant (noncreature, nonland)
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));
        // Creature stays in hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Creature and land cards are excluded from valid choices")
    void creatureAndLandCardsExcluded() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(creature, land, instant)));

        castAndResolveETB();

        // Only index 2 (Peek) should be valid — creature at 0 and land at 1 are excluded
        assertThat(gd.interaction.cardChoice().validIndices()).containsExactly(2);
    }

    @Test
    @DisplayName("Hand with only creatures and lands results in no valid choices")
    void handWithOnlyCreaturesAndLandsNoChoices() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(creature, land)));

        castAndResolveETB();

        // No valid choices, effect completes without prompting
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid choices"));
    }

    @Test
    @DisplayName("Empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());

        castAndResolveETB();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("empty"));
    }

    // ===== Return on source leave =====

    @Test
    @DisplayName("Exiled card returns to hand when Freebooter dies")
    void exiledCardReturnsToHandWhenFreebooterDies() {
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(instant)));

        castAndResolveETB();
        harness.handleCardChosen(player1, 0);

        // Verify card is exiled
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        // Verify exile-return tracking exists
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();

        resetForFollowUpSpell();

        // Kill Freebooter with Lightning Bolt
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID freebooterId = harness.getPermanentId(player1, "Kitesail Freebooter");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, freebooterId);
        harness.passBothPriorities();

        // Freebooter is dead
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Kitesail Freebooter"));

        // Card returns to opponent's hand, NOT the battlefield
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Peek"));
        // Should NOT be on the battlefield (it's an instant, not a permanent)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Peek"));
    }

    @Test
    @DisplayName("Exiled card returns to hand when Freebooter is bounced")
    void exiledCardReturnsToHandWhenFreebooterBounced() {
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(instant)));

        castAndResolveETB();
        harness.handleCardChosen(player1, 0);

        resetForFollowUpSpell();

        // Bounce Freebooter with Unsummon
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        UUID freebooterId = harness.getPermanentId(player1, "Kitesail Freebooter");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, freebooterId);
        harness.passBothPriorities();

        // Card returns to opponent's hand
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Peek"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Peek"));
    }

    @Test
    @DisplayName("Exile tracking is cleaned up after source leaves")
    void exileTrackingCleanedUpAfterSourceLeaves() {
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(instant)));

        castAndResolveETB();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();

        resetForFollowUpSpell();

        // Kill Freebooter
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID freebooterId = harness.getPermanentId(player1, "Kitesail Freebooter");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, freebooterId);
        harness.passBothPriorities();

        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("No exile tracking when opponent's hand has no valid choices")
    void noTrackingWhenNoValidChoices() {
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(creature)));

        castAndResolveETB();

        // Only creatures in hand, no valid choices — no exile tracking
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    // ===== Logging =====

    @Test
    @DisplayName("Hand reveal is logged")
    void handRevealIsLogged() {
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(instant)));

        castAndResolveETB();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("reveals their hand"));
    }

    @Test
    @DisplayName("Exile is logged")
    void exileIsLogged() {
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(instant)));

        castAndResolveETB();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("exiles") && log.contains("Peek"));
    }

    @Test
    @DisplayName("Return to hand is logged when Freebooter dies")
    void returnToHandIsLogged() {
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(instant)));

        castAndResolveETB();
        harness.handleCardChosen(player1, 0);

        resetForFollowUpSpell();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID freebooterId = harness.getPermanentId(player1, "Kitesail Freebooter");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, freebooterId);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("returns to") && log.contains("hand"));
    }
}
