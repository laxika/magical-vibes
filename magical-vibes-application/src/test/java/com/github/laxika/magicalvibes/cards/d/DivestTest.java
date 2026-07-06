package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromTargetHandToDiscardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DivestTest extends BaseCardTest {

    

    @Test
    @DisplayName("Resolving reveals hand and prompts for artifact/creature choice")
    void promptsForCardChoice() {
        Card creature = new GrizzlyBears();
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(creature, instant)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId()).isEqualTo(player1.getId());
        // Only creature (index 0) should be valid, instant (index 1) is not
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Choosing a creature card discards it")
    void choosingCreatureCardDiscardsIt() {
        Card creature = new GrizzlyBears();
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(creature, instant)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Peek");
    }

    @Test
    @DisplayName("Choosing an artifact card discards it")
    void choosingArtifactCardDiscardsIt() {
        Card artifact = new AccordersShield();
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(artifact, instant)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Only artifact (index 0) should be valid
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Accorder's Shield"));
    }

    @Test
    @DisplayName("Instant and sorcery cards are excluded from valid choices")
    void instantAndSorceryExcluded() {
        Card instant = new Peek();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(instant, creature)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Only index 1 (creature) should be valid
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Land cards are excluded from valid choices")
    void landCardsExcluded() {
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(land, creature)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Only index 1 (creature) should be valid
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Selecting a non-artifact/non-creature index is rejected")
    void selectingInvalidTypeIsRejected() {
        Card instant = new Peek();
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(instant, creature)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Hand with no artifacts or creatures results in no valid choices")
    void handWithNoValidTypesNoChoices() {
        Card instant = new Peek();
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(instant, land)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid choices"));
    }

    @Test
    @DisplayName("Resolving against empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("empty"));
    }

    @Test
    @DisplayName("Divest goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(creature)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Divest"));
    }

    @Test
    @DisplayName("Hand reveal is logged")
    void handRevealIsLogged() {
        Card creature = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(creature)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("reveals their hand"));
    }
}
