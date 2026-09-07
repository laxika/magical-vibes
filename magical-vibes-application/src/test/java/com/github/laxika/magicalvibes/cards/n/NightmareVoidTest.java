package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({NightmareVoid.class, Forest.class, GrizzlyBears.class})
class NightmareVoidTest extends BaseCardTest {

    @Test
    @DisplayName("Target player reveals their hand and discards the chosen card")
    void choosesCardToDiscard() {
        Card discarded = new GrizzlyBears();
        Card remaining = new Forest();
        harness.setHand(player2, List.of(discarded, remaining));
        castNightmareVoid(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("May dredge Nightmare Void instead of drawing")
    void dredgesInsteadOfDrawing() {
        NightmareVoid nightmareVoid = new NightmareVoid();
        List<Card> milled = List.of(new Forest(), new GrizzlyBears());
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
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears()));

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
        var permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NightmareVoid()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castNightmareVoid(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new NightmareVoid()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void resolveDraw() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
