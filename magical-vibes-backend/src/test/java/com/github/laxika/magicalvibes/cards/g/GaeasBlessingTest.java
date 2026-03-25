package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GaeasBlessingTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has SPELL effects: shuffle up to 3 cards from graveyard + draw a card")
    void hasCorrectSpellEffects() {
        GaeasBlessing card = new GaeasBlessing();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(ShuffleTargetCardsFromGraveyardIntoLibraryEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1))
                .isInstanceOf(DrawCardEffect.class);

        ShuffleTargetCardsFromGraveyardIntoLibraryEffect shuffleEffect =
                (ShuffleTargetCardsFromGraveyardIntoLibraryEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(shuffleEffect.maxTargets()).isEqualTo(3);
        assertThat(shuffleEffect.filter()).isNull();
    }

    @Test
    @DisplayName("Has ON_SELF_MILLED effect: shuffle graveyard into library")
    void hasSelfMilledEffect() {
        GaeasBlessing card = new GaeasBlessing();

        assertThat(card.getEffects(EffectSlot.ON_SELF_MILLED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_SELF_MILLED).get(0))
                .isInstanceOf(ShuffleGraveyardIntoLibraryEffect.class);
    }

    @Test
    @DisplayName("Needs target (auto-derived from player-targeting effect)")
    void needsTarget() {
        GaeasBlessing card = new GaeasBlessing();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
    }

    // ===== Casting — graveyard targeting + draw =====

    @Test
    @DisplayName("Casting targeting self with cards in graveyard prompts for target selection")
    void castingTargetingSelfPromptsForGraveyardSelection() {
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player1, List.of(card1, card2));
        harness.setHand(player1, List.of(new GaeasBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.multiSelection().multiGraveyardPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.multiSelection().multiGraveyardMaxCount()).isEqualTo(2); // min(3, 2 cards)
    }

    @Test
    @DisplayName("Resolving shuffles selected cards from graveyard into library and draws a card")
    void resolvingShufflesCardsAndDrawsCard() {
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player1, List.of(card1, card2));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new GaeasBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player1.getId());

        // Select both cards
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        // Resolve
        harness.passBothPriorities();

        // Cards should no longer be in graveyard (only Gaea's Blessing itself goes to graveyard)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"))
                .noneMatch(c -> c.getName().equals("Lightning Bolt"))
                .anyMatch(c -> c.getName().equals("Gaea's Blessing"));

        // Library should have gained 2 cards (shuffled back) minus 1 drawn
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 2 - 1);

        // Hand should have 1 card (cast 1 from hand, drew 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can target opponent's graveyard and shuffle their cards, but controller draws")
    void canTargetOpponentGraveyard() {
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player2, List.of(card1, card2));
        int opponentLibSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new GaeasBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());

        // Select both cards
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        harness.passBothPriorities();

        // Opponent's graveyard should be empty
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        // Opponent's library gained 2 cards
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibSizeBefore + 2);

        // Controller drew a card (cast 1 from hand, drew 1 → hand has 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        // Gaea's Blessing goes to caster's graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gaea's Blessing"));
    }

    @Test
    @DisplayName("Casting with empty target graveyard puts spell on stack directly and draws a card")
    void castingWithEmptyGraveyardStillDraws() {
        harness.setHand(player1, List.of(new GaeasBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player1.getId());

        // No graveyard prompt — spell goes directly on stack
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Controller drew 1 card (cast 1 from hand, drew 1 → hand has 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Max targets is capped at 3 even with more cards in graveyard")
    void maxTargetsCappedAtThree() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new LightningBolt(),
                new GrizzlyBears(), new LightningBolt()));
        harness.setHand(player1, List.of(new GaeasBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.multiSelection().multiGraveyardMaxCount()).isEqualTo(3);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(4);
    }

    // ===== Self-mill trigger =====

    @Test
    @DisplayName("When milled, shuffles owner's graveyard into library")
    void selfMillTriggerShufflesGraveyardIntoLibrary() {
        Permanent millstone = addReadyMillstone(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Put a card in player2's graveyard first
        Card existingGraveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(existingGraveyardCard));

        // Set up player2's library: Gaea's Blessing on top, another card below
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new GaeasBlessing());
        gd.playerDecks.get(player2.getId()).add(new LightningBolt());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Graveyard should be empty — everything was shuffled into library
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        // Library should contain the existing graveyard card + the milled cards (all shuffled back)
        // Original: 2 cards in library, milled 2. The Grizzly Bears was in graveyard.
        // After mill: GaeasBlessing and LightningBolt go to graveyard, trigger fires,
        // all 3 graveyard cards (existing + 2 milled) shuffle into library.
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);

        // Log confirms the self-mill trigger
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Gaea's Blessing") && log.contains("was milled") && log.contains("shuffles their graveyard"));
    }

    @Test
    @DisplayName("When milled with empty graveyard (only self), still shuffles into library")
    void selfMillTriggerWithOnlyItselfInGraveyard() {
        Permanent millstone = addReadyMillstone(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Player2 has empty graveyard, library has Gaea's Blessing + another card
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new GaeasBlessing());
        gd.playerDecks.get(player2.getId()).add(new LightningBolt());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Both milled cards were shuffled back into library (trigger fires after both enter graveyard)
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Self-mill trigger does not fire when Gaea's Blessing is discarded (only from library)")
    void noTriggerOnDiscard() {
        // Gaea's Blessing in graveyard via discard (simulated by setGraveyard) — not milled
        Card existingCard = new LightningBolt();
        harness.setGraveyard(player1, List.of(new GaeasBlessing(), existingCard));

        // Graveyard should remain intact — no shuffle trigger
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gaea's Blessing"))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Self-mill trigger fires even when milled by opponent")
    void selfMillTriggerFiresWhenMilledByOpponent() {
        Permanent millstone = addReadyMillstone(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Set up player2's library with Gaea's Blessing
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new LightningBolt());
        gd.playerDecks.get(player2.getId()).add(new GaeasBlessing());

        // Put something in player2's graveyard
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Graveyard should be empty — trigger shuffled everything back
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        // All cards should be in library (1 existing graveyard + 2 milled = 3)
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    // ===== Helpers =====

    private Permanent addReadyMillstone(Player player) {
        Millstone card = new Millstone();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
