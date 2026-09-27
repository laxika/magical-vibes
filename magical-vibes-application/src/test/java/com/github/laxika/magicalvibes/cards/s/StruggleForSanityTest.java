package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StruggleForSanity.class, Forest.class, HumbleBudoka.class, Island.class, LanternKami.class})
class StruggleForSanityTest extends BaseCardTest {

    private PendingInteraction.AlternatingHandExileChoice activeChoice() {
        return gd.interaction.activeInteraction(PendingInteraction.AlternatingHandExileChoice.class);
    }

    private void castStruggle() {
        harness.setHand(player1, List.of(new StruggleForSanity()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The targeted player picks first")
    void targetPicksFirst() {
        harness.setHand(player2, List.of(new LanternKami(), new HumbleBudoka()));

        castStruggle();

        PendingInteraction.AlternatingHandExileChoice choice = activeChoice();
        assertThat(choice).isNotNull();
        assertThat(choice.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.validIndices()).containsExactly(0, 1);
        assertThat(gameLogContains("reveals their hand")).isTrue();
    }

    @Test
    @DisplayName("Picks alternate between the target and the controller")
    void picksAlternate() {
        harness.setHand(player2, List.of(
                new LanternKami(), new HumbleBudoka(), new Forest(), new Island()));

        castStruggle();

        harness.handleCardChosen(player2, 0);
        assertThat(activeChoice().decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(activeChoice().validIndices()).containsExactly(0, 1, 2);

        harness.handleCardChosen(player1, 0);
        assertThat(activeChoice().decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(activeChoice().validIndices()).containsExactly(0, 1);
    }

    @Test
    @DisplayName("Target keeps the cards they exiled; the controller's picks hit the graveyard")
    void keptAndBinnedPiles() {
        harness.setHand(player2, List.of(
                new LanternKami(), new HumbleBudoka(), new Forest(), new Island()));

        castStruggle();

        // player2 exiles Lantern Kami, player1 exiles Humble Budoka,
        // player2 exiles Forest, player1 exiles Island.
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Lantern Kami", "Forest");
        harness.assertInGraveyard(player2, "Humble Budoka");
        harness.assertInGraveyard(player2, "Island");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("An odd hand size leaves the last card to the target, who keeps it")
    void oddHandSizeLastPickIsTheTargets() {
        harness.setHand(player2, List.of(new LanternKami(), new HumbleBudoka(), new Forest()));

        castStruggle();

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Lantern Kami", "Forest");
        harness.assertInGraveyard(player2, "Humble Budoka");
    }

    @Test
    @DisplayName("Cards sit in exile between picks")
    void cardsAreInExileBetweenPicks() {
        harness.setHand(player2, List.of(new LanternKami(), new HumbleBudoka()));

        castStruggle();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.exiledCards).hasSize(1);
        assertThat(gd.exiledCards.getFirst().card().getName()).isEqualTo("Lantern Kami");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Resolving against an empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());

        castStruggle();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("empty")).isTrue();
    }

    @Test
    @DisplayName("The spell cannot target its controller")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new StruggleForSanity()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("The wrong player cannot answer the current pick")
    void wrongPlayerCannotChoose() {
        harness.setHand(player2, List.of(new LanternKami(), new HumbleBudoka()));

        castStruggle();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("An out-of-range card index is rejected")
    void invalidIndexRejected() {
        harness.setHand(player2, List.of(new LanternKami(), new HumbleBudoka()));

        castStruggle();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 7))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }
}
