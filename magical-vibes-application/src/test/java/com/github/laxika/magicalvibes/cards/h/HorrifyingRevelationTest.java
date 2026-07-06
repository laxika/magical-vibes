package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HorrifyingRevelationTest extends BaseCardTest {

    // ===== Resolving against opponent =====

    @Test
    @DisplayName("Target opponent discards a card then mills a card")
    void targetOpponentDiscardsAndMills() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new HorrifyingRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Discard interaction is awaited
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        // Grizzly Bears discarded + 1 card milled
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Target opponent with empty hand still mills a card")
    void emptyHandStillMills() {
        harness.setHand(player2, new ArrayList<>());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new HorrifyingRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // No discard prompt since hand is empty, mill still happens
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
    }

    // ===== Targeting self =====

    @Test
    @DisplayName("Can target yourself to discard and mill")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new HorrifyingRevelation(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Self-discard interaction
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        // Grizzly Bears discarded (index 0 of remaining hand after spell was cast)
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    // ===== Sorcery goes to graveyard =====

    @Test
    @DisplayName("Goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player1, List.of(new HorrifyingRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Horrifying Revelation"));
        assertThat(gd.stack).isEmpty();
    }
}
