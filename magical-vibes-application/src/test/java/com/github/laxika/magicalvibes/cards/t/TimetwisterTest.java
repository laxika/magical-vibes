package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Timetwister.class, GrizzlyBears.class})
class TimetwisterTest extends BaseCardTest {

    @Test
    @DisplayName("Each player shuffles hand and graveyard away and draws seven")
    void eachPlayerShufflesAndDrawsSeven() {
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new Timetwister()));
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player1, deckOf(20));
        harness.setLibrary(player2, deckOf(20));

        cast();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        List<Card> libraryAndHand = new ArrayList<>(gd.playerDecks.get(player2.getId()));
        libraryAndHand.addAll(gd.playerHands.get(player2.getId()));
        assertThat(libraryAndHand).contains(handCard, graveyardCard).hasSize(22);
    }

    @Test
    @DisplayName("Both players recycle exactly seven cards while the resolving Timetwister stays out")
    void recyclesBothPlayersZonesWithoutRecyclingItself() {
        Timetwister spell = new Timetwister();
        Card casterHand = new GrizzlyBears();
        Card casterGraveyard = new GrizzlyBears();
        Card opponentHand = new GrizzlyBears();
        Card opponentGraveyard = new GrizzlyBears();
        List<Card> casterLibrary = deckOf(5);
        List<Card> opponentLibrary = deckOf(5);
        List<Card> casterPool = new ArrayList<>(casterLibrary);
        casterPool.add(casterHand);
        casterPool.add(casterGraveyard);
        List<Card> opponentPool = new ArrayList<>(opponentLibrary);
        opponentPool.add(opponentHand);
        opponentPool.add(opponentGraveyard);
        harness.setHand(player1, List.of(spell, casterHand));
        harness.setHand(player2, List.of(opponentHand));
        harness.setGraveyard(player1, List.of(casterGraveyard));
        harness.setGraveyard(player2, List.of(opponentGraveyard));
        harness.setLibrary(player1, casterLibrary);
        harness.setLibrary(player2, opponentLibrary);

        cast();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(casterPool);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrderElementsOf(opponentPool);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Timetwister leaves battlefield and exile cards in their zones")
    void leavesBattlefieldAndExileAlone() {
        Card casterPermanent = new GrizzlyBears();
        Card opponentPermanent = new GrizzlyBears();
        Card casterExile = new GrizzlyBears();
        Card opponentExile = new GrizzlyBears();
        harness.addToBattlefield(player1, casterPermanent);
        harness.addToBattlefield(player2, opponentPermanent);
        harness.setExile(player1, List.of(casterExile));
        harness.setExile(player2, List.of(opponentExile));
        harness.setHand(player1, List.of(new Timetwister()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, deckOf(7));
        harness.setLibrary(player2, deckOf(7));

        cast();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard()).containsExactly(casterPermanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(p -> p.getCard()).containsExactly(opponentPermanent);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7).doesNotContain(casterExile, opponentExile);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7).doesNotContain(casterExile, opponentExile);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card()).containsExactlyInAnyOrder(casterExile, opponentExile);
    }

    private void cast() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private List<Card> deckOf(int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new GrizzlyBears());
        }
        return deck;
    }
}
