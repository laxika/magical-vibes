package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HitchclawRecluse;
import com.github.laxika.magicalvibes.cards.i.InfernalScarring;
import com.github.laxika.magicalvibes.cards.n.NotionThief;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheGreatAurora.class, Forest.class, HitchclawRecluse.class, InfernalScarring.class, NotionThief.class})
class TheGreatAuroraTest extends BaseCardTest {

    private static List<Card> forests(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }

    private void castAurora() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 9);
        harness.castAndResolveSorcery(player1, 0, List.of());
    }

    @Test
    @DisplayName("Each player shuffles their hand and owned permanents away and draws that many cards")
    void shufflesHandAndPermanentsAndDrawsThatMany() {
        harness.setHand(player1, List.of(new TheGreatAurora(), new Forest(), new Forest()));
        harness.addToBattlefield(player1, new HitchclawRecluse());
        harness.setLibrary(player1, forests(5));

        harness.setHand(player2, List.of(new HitchclawRecluse()));
        harness.setLibrary(player2, forests(4));

        castAurora();

        // player1 shuffled two hand cards plus one permanent: library 5 + 3 = 8, draws 3.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        // player2 shuffled one hand card and owned nothing: library 4 + 1 = 5, draws 1.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Each player may put any number of land cards from their new hand onto the battlefield")
    void eachPlayerMayPutLandsOntoBattlefield() {
        harness.setHand(player1, List.of(new TheGreatAurora(), new Forest()));
        harness.setLibrary(player1, forests(3));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, forests(3));

        castAurora();

        // Active player chooses their whole all-land hand; entry waits for the other choice.
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutLandsFromHandChoice.class);
        harness.handleMultipleCardsChosen(player1, handCardIds(player1));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        // The non-active player declines, then the selected lands enter together.
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutLandsFromHandChoice.class);
        harness.handleMultipleCardsChosen(player2, List.of());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The Great Aurora exiles itself instead of going to the graveyard")
    void exilesItself() {
        harness.setHand(player1, List.of(new TheGreatAurora()));
        harness.setLibrary(player1, forests(3));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, forests(3));

        castAurora();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName).contains("The Great Aurora");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).doesNotContain("The Great Aurora");
    }

    @Test
    @DisplayName("All players' permanents are shuffled away before anyone draws")
    void opposingDrawReplacementIsGoneBeforeDrawing() {
        harness.setHand(player1, List.of(new TheGreatAurora(), new Forest(), new Forest()));
        harness.setLibrary(player1, forests(5));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, forests(5));
        harness.addToBattlefield(player2, new NotionThief());

        castAurora();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Both players' chosen lands wait until every player has chosen")
    void bothPlayersChooseBeforeLandsEnter() {
        harness.setHand(player1, List.of(new TheGreatAurora(), new Forest(), new Forest()));
        harness.setLibrary(player1, forests(5));
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, forests(5));

        castAurora();
        harness.handleMultipleCardsChosen(player1, handCardIds(player1));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        harness.handleMultipleCardsChosen(player2, handCardIds(player2));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2)
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName).contains("The Great Aurora");
    }

    @Test
    @DisplayName("A player may put only some lands from their new hand onto the battlefield")
    void mayChooseOnlySomeLands() {
        harness.setHand(player1, List.of(new TheGreatAurora(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, forests(5));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, forests(5));

        castAurora();
        harness.handleMultipleCardsChosen(player1, List.of(handCardIds(player1).getFirst()));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName).contains("The Great Aurora");
    }

    @Test
    @DisplayName("A stolen permanent counts for its owner rather than its controller")
    void stolenPermanentCountsForOwner() {
        harness.setHand(player1, List.of(new TheGreatAurora()));
        harness.setLibrary(player1, forests(5));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, forests(5));
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, new HitchclawRecluse());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());

        castAurora();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        List<Card> ownerCards = new ArrayList<>(gd.playerDecks.get(player2.getId()));
        ownerCards.addAll(gd.playerHands.get(player2.getId()));
        assertThat(ownerCards).extracting(Card::getId).contains(stolen.getCard().getId());
    }

    @Test
    @DisplayName("Tokens count toward the draw but do not remain in the library")
    void tokensCountTowardDraw() {
        harness.setHand(player1, List.of(new TheGreatAurora()));
        harness.setLibrary(player1, forests(5));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, forests(5));
        Card token = new HitchclawRecluse();
        token.setToken(true);
        harness.addToBattlefield(player1, token);

        castAurora();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4)
                .noneMatch(Card::isToken);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opposing aura is shuffled away with the creature it enchants")
    void opposingAuraIsShuffledRatherThanPutInGraveyard() {
        harness.setHand(player1, List.of(new TheGreatAurora()));
        harness.setLibrary(player1, forests(5));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, forests(5));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HitchclawRecluse());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new InfernalScarring());
        aura.setAttachedTo(creature.getId());

        castAurora();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        List<Card> ownerCards = new ArrayList<>(gd.playerDecks.get(player2.getId()));
        ownerCards.addAll(gd.playerHands.get(player2.getId()));
        assertThat(ownerCards).extracting(Card::getId).contains(aura.getCard().getId());
    }

    private List<UUID> handCardIds(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerHands.get(player.getId()).stream().map(Card::getId).toList();
    }
}
