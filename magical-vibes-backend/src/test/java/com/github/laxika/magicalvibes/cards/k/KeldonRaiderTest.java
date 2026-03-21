package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeldonRaiderTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Keldon Raider has ON_ENTER_BATTLEFIELD MayEffect wrapping DiscardAndDrawCardEffect")
    void hasCorrectEffect() {
        KeldonRaider card = new KeldonRaider();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(may.wrapped()).isInstanceOf(DiscardAndDrawCardEffect.class);
    }

    // ===== ETB trigger: accept may, discard then draw =====

    @Test
    @DisplayName("When Keldon Raider enters, accepting may prompts discard then draws a card")
    void acceptMayDiscardsThenDraws() {
        setDeck(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Card bearInHand = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new KeldonRaider(), bearInHand)));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        // Resolve the creature spell → Keldon Raider enters
        harness.passBothPriorities();
        // Resolve ETB triggered ability → MayEffect prompts player
        harness.passBothPriorities();

        // May ability prompt
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Should now be awaiting discard choice (discard happens BEFORE draw)
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);

        // Discard the remaining card (Grizzly Bears at index 0)
        harness.handleCardChosen(player1, 0);

        // The Grizzly Bears should be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Should have drawn a card (Forest from deck) — hand size = 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    // ===== ETB trigger: decline may =====

    @Test
    @DisplayName("When Keldon Raider enters, declining may does not discard or draw")
    void declineMayDoesNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Card bearInHand = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new KeldonRaider(), bearInHand)));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB → may prompt

        harness.handleMayAbilityChosen(player1, false);

        // Hand should still have the Grizzly Bears (no discard, no draw)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== ETB trigger: empty hand, accept may =====

    @Test
    @DisplayName("When Keldon Raider enters with empty hand, accepting may does nothing (cannot discard)")
    void acceptMayWithEmptyHandDoesNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, new ArrayList<>(List.of(new KeldonRaider())));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB → may prompt

        harness.handleMayAbilityChosen(player1, true);

        // Hand is empty — no discard possible, so no draw either
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    // ===== Helpers =====

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
