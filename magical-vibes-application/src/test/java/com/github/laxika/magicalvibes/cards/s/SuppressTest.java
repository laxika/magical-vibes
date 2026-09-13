package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Index;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Suppress.class, Index.class})
class SuppressTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles only the target player's hand face down")
    void exilesTargetHandFaceDown() {
        Card casterHandCard = new Index();
        List<Card> targetHand = List.of(new Index(), new Index());
        harness.setHand(player1, List.of(new Suppress(), casterHandCard));
        harness.setHand(player2, targetHand);
        castSuppress();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(casterHandCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .extracting(ExiledCardEntry::card)
                .containsExactlyInAnyOrderElementsOf(targetHand);
        assertThat(gd.exiledCards).allMatch(ExiledCardEntry::faceDown);
    }

    @Test
    @DisplayName("Returns the cards at the target player's next end step without discarding new cards")
    void returnsAtTargetPlayersNextEndStep() {
        Card exiledCard = new Index();
        Card newHandCard = new Index();
        Card drawnCard = new Index();
        harness.setHand(player1, List.of(new Suppress()));
        harness.setHand(player2, List.of(exiledCard));
        harness.setLibrary(player2, List.of(drawnCard));
        castSuppress();
        harness.setHand(player2, List.of(newHandCard));

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(exiledCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(newHandCard);

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactlyInAnyOrder(newHandCard, exiledCard, drawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not return cards added after resolving against an empty hand")
    void emptyTargetHandDoesNotReturnLaterCards() {
        Card newHandCard = new Index();
        Card drawnCard = new Index();
        harness.setHand(player1, List.of(new Suppress()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));
        castSuppress();
        harness.setHand(player2, List.of(newHandCard));

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactlyInAnyOrder(newHandCard, drawnCard);
    }

    @Test
    @DisplayName("Can target the caster's hand")
    void canTargetCaster() {
        Card exiledCard = new Index();
        Card drawnCard = new Index();
        harness.setHand(player1, List.of(new Suppress(), exiledCard));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        castSuppress(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .extracting(ExiledCardEntry::card)
                .containsExactly(exiledCard);
        assertThat(gd.exiledCards).allMatch(ExiledCardEntry::faceDown);

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(exiledCard);

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(exiledCard, drawnCard);
    }

    private void castSuppress() {
        castSuppress(player2);
    }

    private void castSuppress(Player targetPlayer) {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player1, 0, targetPlayer.getId());
        harness.passBothPriorities();
    }
}
