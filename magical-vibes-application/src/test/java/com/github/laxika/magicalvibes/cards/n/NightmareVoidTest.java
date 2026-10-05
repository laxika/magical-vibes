package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({NightmareVoid.class, Forest.class})
class NightmareVoidTest extends BaseCardTest {

    @Test
    @DisplayName("Target player reveals their hand and discards the chosen card")
    void choosesCardToDiscard() {
        Card discarded = new Forest();
        Card remaining = new NightmareVoid();
        harness.setHand(player2, List.of(discarded, remaining));
        castNightmareVoid(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 1);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        NightmareVoid spell = new NightmareVoid();
        Card discarded = new Forest();
        Card remaining = new Forest();
        harness.setHand(player1, List.of(spell, discarded, remaining));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Does nothing when the target player's hand is empty")
    void emptyTargetHandDoesNotCreateChoice() {
        harness.setHand(player2, List.of());
        castNightmareVoid(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("May dredge Nightmare Void instead of drawing")
    void dredgesInsteadOfDrawing() {
        NightmareVoid nightmareVoid = new NightmareVoid();
        List<Card> milled = List.of(new Forest(), new Forest());
        harness.setGraveyard(player1, List.of(nightmareVoid));
        harness.setLibrary(player1, milled);

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(nightmareVoid);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(milled);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Can decline dredge and draw normally")
    void declinesDredge() {
        NightmareVoid nightmareVoid = new NightmareVoid();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(nightmareVoid));
        harness.setLibrary(player1, List.of(topCard, new Forest()));

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nightmareVoid);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not offer dredge when the library has too few cards")
    void cannotDredgeWithTooFewLibraryCards() {
        NightmareVoid nightmareVoid = new NightmareVoid();
        Card topCard = new Forest();
        harness.setGraveyard(player1, List.of(nightmareVoid));
        harness.setLibrary(player1, List.of(topCard));

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nightmareVoid);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        var permanent = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new NightmareVoid()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dredging mills exactly two cards and returns only the chosen copy")
    void dredgesOnlyChosenCopy() {
        NightmareVoid unchosen = new NightmareVoid();
        NightmareVoid chosen = new NightmareVoid();
        Card first = new Forest();
        Card second = new Forest();
        Card remaining = new Forest();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(unchosen, chosen));
        harness.setLibrary(player1, List.of(first, second, remaining));

        resolveDraw();
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unchosen, first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.cardsDrawnThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("An opponent's Nightmare Void cannot replace your draw")
    void opponentsGraveyardDoesNotReplaceDraw() {
        NightmareVoid nightmareVoid = new NightmareVoid();
        Card top = new Forest();
        Card remaining = new Forest();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(nightmareVoid));
        harness.setLibrary(player1, List.of(top, remaining));

        resolveDraw();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(nightmareVoid);
        assertThat(gd.cardsDrawnThisTurn.get(player1.getId())).isEqualTo(1);
    }

    private void castNightmareVoid(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new NightmareVoid()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
