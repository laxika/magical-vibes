package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsReturnSelfIfCardTypeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PsychicMiasmaTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Psychic Miasma has correct effect")
    void hasCorrectEffect() {
        PsychicMiasma card = new PsychicMiasma();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(TargetPlayerDiscardsReturnSelfIfCardTypeEffect.class);
        TargetPlayerDiscardsReturnSelfIfCardTypeEffect effect =
                (TargetPlayerDiscardsReturnSelfIfCardTypeEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.returnIfType()).isEqualTo(CardType.LAND);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Psychic Miasma puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PsychicMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Psychic Miasma");
    }

    // ===== Discarding a non-land card =====

    @Test
    @DisplayName("Discarding a non-land card sends Psychic Miasma to graveyard")
    void discardingNonLandGoesToGraveyard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player1, List.of(new PsychicMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0); // discard Grizzly Bears

        // Psychic Miasma should be in caster's graveyard (not returned to hand)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Psychic Miasma"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Psychic Miasma"));
        // Target discarded their card
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    // ===== Discarding a land card =====

    @Test
    @DisplayName("Discarding a land card returns Psychic Miasma to owner's hand")
    void discardingLandReturnsToHand() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, List.of(new PsychicMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0); // discard Forest (a land)

        // Psychic Miasma should be returned to caster's hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Psychic Miasma"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Psychic Miasma"));
        // Target discarded the land
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Return to hand is logged")
    void returnToHandIsLogged() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, List.of(new PsychicMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Psychic Miasma") && log.contains("returned to its owner's hand"));
    }

    @Test
    @DisplayName("Spell is not in graveyard while awaiting discard choice (deferred disposition)")
    void spellNotInGraveyardWhileAwaitingDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, List.of(new PsychicMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // While awaiting discard choice, spell should NOT be in any zone yet
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Psychic Miasma"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Psychic Miasma"));
    }

    // ===== Target with empty hand =====

    @Test
    @DisplayName("Target with empty hand results in no discard and spell goes to graveyard")
    void targetWithEmptyHandNoDiscard() {
        harness.setHand(player2, new ArrayList<>());
        harness.setHand(player1, List.of(new PsychicMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // No discard prompt
        assertThat(gd.interaction.awaitingInputType()).isNull();
        // Psychic Miasma goes to graveyard (no land was discarded)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Psychic Miasma"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Psychic Miasma"));
    }

    // ===== Discarding from multiple options =====

    @Test
    @DisplayName("Choosing non-land when both land and non-land available keeps spell in graveyard")
    void choosingNonLandFromMultipleOptions() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        harness.setHand(player1, List.of(new PsychicMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0); // discard Grizzly Bears (non-land)

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Psychic Miasma"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Psychic Miasma"));
        // One card remains in opponent's hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Choosing land when both land and non-land available returns spell to hand")
    void choosingLandFromMultipleOptions() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        harness.setHand(player1, List.of(new PsychicMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 1); // discard Forest (land)

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Psychic Miasma"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Psychic Miasma"));
        // One card remains in opponent's hand
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }
}
