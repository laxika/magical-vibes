package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Divest.class, BalothGorger.class, ShortSword.class, Opt.class, Forest.class})
class DivestTest extends BaseCardTest {

    

    @Test
    @DisplayName("Resolving reveals hand and prompts for artifact/creature choice")
    void promptsForCardChoice() {
        Card creature = new BalothGorger();
        Card instant = new Opt();
        harness.setHand(player2, new ArrayList<>(List.of(creature, instant)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId()).isEqualTo(player1.getId());
        // Only creature (index 0) should be valid, instant (index 1) is not
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Choosing a creature card discards it")
    void choosingCreatureCardDiscardsIt() {
        Card creature = new BalothGorger();
        Card instant = new Opt();
        harness.setHand(player2, new ArrayList<>(List.of(creature, instant)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Baloth Gorger");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Opt");
    }

    @Test
    @DisplayName("Choosing an artifact card discards it")
    void choosingArtifactCardDiscardsIt() {
        Card artifact = new ShortSword();
        Card instant = new Opt();
        harness.setHand(player2, new ArrayList<>(List.of(artifact, instant)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Only artifact (index 0) should be valid
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Short Sword");
    }

    @Test
    @DisplayName("Instant and sorcery cards are excluded from valid choices")
    void instantAndSorceryExcluded() {
        Card instant = new Opt();
        Card creature = new BalothGorger();
        harness.setHand(player2, new ArrayList<>(List.of(instant, creature)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Only index 1 (creature) should be valid
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Land cards are excluded from valid choices")
    void landCardsExcluded() {
        Card land = new Forest();
        Card creature = new BalothGorger();
        harness.setHand(player2, new ArrayList<>(List.of(land, creature)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Only index 1 (creature) should be valid
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Selecting a non-artifact/non-creature index is rejected")
    void selectingInvalidTypeIsRejected() {
        Card instant = new Opt();
        Card creature = new BalothGorger();
        harness.setHand(player2, new ArrayList<>(List.of(instant, creature)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Hand with no artifacts or creatures results in no valid choices")
    void handWithNoValidTypesNoChoices() {
        Card instant = new Opt();
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(instant, land)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no valid choices"));
    }

    @Test
    @DisplayName("Resolving against empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("empty"));
    }

    @Test
    @DisplayName("Divest goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card creature = new BalothGorger();
        harness.setHand(player2, new ArrayList<>(List.of(creature)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Divest");
    }

    @Test
    @DisplayName("Hand reveal is logged")
    void handRevealIsLogged() {
        Card creature = new BalothGorger();
        harness.setHand(player2, new ArrayList<>(List.of(creature)));

        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("reveals their hand"));
    }

    @Test
    @DisplayName("The caster chooses exactly one eligible card and cannot decline")
    void choosesExactlyOneEligibleCard() {
        Card creature = new BalothGorger();
        Card artifact = new ShortSword();
        Card instant = new Opt();
        harness.setHand(player2, List.of(creature, artifact, instant));
        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(0, 1);
        assertThatThrownBy(() -> harness.handleCardChosen(player2, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature, instant);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(artifact);
        harness.assertInGraveyard(player1, "Divest");
    }

    @Test
    @DisplayName("Divest can target its caster")
    void canTargetSelf() {
        Card creature = new BalothGorger();
        Card instant = new Opt();
        harness.setHand(player1, List.of(new Divest(), creature, instant));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
        harness.assertInGraveyard(player1, "Baloth Gorger");
        harness.assertInGraveyard(player1, "Divest");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sorceries are excluded while eligible artifacts remain selectable")
    void sorceryExcluded() {
        Card sorcery = new Divest();
        Card artifact = new ShortSword();
        harness.setHand(player2, List.of(sorcery, artifact));
        harness.setHand(player1, List.of(new Divest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class)
                .validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(sorcery);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(artifact);
    }
}
