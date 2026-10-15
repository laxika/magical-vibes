package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.k.KarnLiberated;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlistenerElf;
import com.github.laxika.magicalvibes.cards.g.GutShot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Despise.class, GlistenerElf.class, GutShot.class, KarnLiberated.class, Forest.class})
class DespiseTest extends BaseCardTest {

    @Test
    @DisplayName("Despise cannot target its caster")
    void cannotTargetCaster() {
        harness.setHand(player1, List.of(new Despise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Despise requires an opponent target")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new Despise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Caster must choose exactly one eligible card")
    void mustChooseExactlyOneEligibleCard() {
        Card creature = new GlistenerElf();
        Card planeswalker = new KarnLiberated();
        harness.setHand(player2, List.of(creature, planeswalker, new GutShot(), new Despise()));
        harness.setHand(player1, List.of(new Despise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 1);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(planeswalker);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3).contains(creature);
        harness.assertInHand(player2, "Gut Shot");
        harness.assertInHand(player2, "Despise");
    }

    @Test
    @DisplayName("Resolving reveals hand and prompts for creature/planeswalker choice")
    void promptsForCardChoice() {
        Card creature = new GlistenerElf();
        Card instant = new GutShot();
        harness.setHand(player2, List.of(creature, instant));

        harness.setHand(player1, List.of(new Despise()));
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
        Card creature = new GlistenerElf();
        Card instant = new GutShot();
        harness.setHand(player2, List.of(creature, instant));

        harness.setHand(player1, List.of(new Despise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Glistener Elf");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Gut Shot");
    }

    @Test
    @DisplayName("Choosing a planeswalker card discards it")
    void choosingPlaneswalkerCardDiscardsIt() {
        Card planeswalker = new KarnLiberated();
        Card instant = new GutShot();
        harness.setHand(player2, List.of(planeswalker, instant));

        harness.setHand(player1, List.of(new Despise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Only planeswalker (index 0) should be valid
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Karn Liberated");
    }

    @Test
    @DisplayName("Instant cards are excluded from valid choices")
    void instantExcluded() {
        Card instant = new GutShot();
        Card creature = new GlistenerElf();
        harness.setHand(player2, List.of(instant, creature));

        harness.setHand(player1, List.of(new Despise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Only index 1 (creature) should be valid
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Land cards are excluded from valid choices")
    void landCardsExcluded() {
        Card land = new Forest();
        Card creature = new GlistenerElf();
        harness.setHand(player2, List.of(land, creature));

        harness.setHand(player1, List.of(new Despise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Only index 1 (creature) should be valid
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Selecting a non-creature/non-planeswalker index is rejected")
    void selectingInvalidTypeIsRejected() {
        Card instant = new GutShot();
        Card creature = new GlistenerElf();
        harness.setHand(player2, List.of(instant, creature));

        harness.setHand(player1, List.of(new Despise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Hand with no creatures or planeswalkers results in no valid choices")
    void handWithNoValidTypesNoChoices() {
        Card instant = new GutShot();
        Card land = new Forest();
        harness.setHand(player2, List.of(instant, land));

        harness.setHand(player1, List.of(new Despise()));
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
        harness.setHand(player1, List.of(new Despise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("empty"));
    }

    @Test
    @DisplayName("Despise goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card creature = new GlistenerElf();
        harness.setHand(player2, List.of(creature));

        harness.setHand(player1, List.of(new Despise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Despise");
    }

    @Test
    @DisplayName("Hand reveal is logged")
    void handRevealIsLogged() {
        Card creature = new GlistenerElf();
        harness.setHand(player2, List.of(creature));

        harness.setHand(player1, List.of(new Despise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("reveals their hand"));
    }
}
