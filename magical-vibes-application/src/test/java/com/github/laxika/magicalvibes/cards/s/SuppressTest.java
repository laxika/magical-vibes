package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Suppress.class, Forest.class})
class SuppressTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles only the target player's hand face down")
    void exilesTargetHandFaceDown() {
        Card casterHandCard = new Forest();
        List<Card> targetHand = List.of(new Forest(), new Forest());
        harness.setHand(player1, new ArrayList<>(List.of(new Suppress(), casterHandCard)));
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
        Card exiledCard = new Forest();
        Card newHandCard = new Forest();
        Card drawnCard = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new Suppress())));
        harness.setHand(player2, new ArrayList<>(List.of(exiledCard)));
        harness.setLibrary(player2, List.of(drawnCard));
        castSuppress();
        harness.setHand(player2, new ArrayList<>(List.of(newHandCard)));

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

    private void castSuppress() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
