package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WandOfVertebrae.class, Forest.class, GrizzlyBears.class, CosisTrickster.class})
class WandOfVertebraeTest extends BaseCardTest {

    @Test
    void millsOneCardFromControllersLibraryWithoutTargeting() {
        addReadyWand(player1);
        Card topCard = new Forest();
        Card nextCard = new GrizzlyBears();
        Card opponentCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentCard);
    }

    @Test
    void exilesItselfAndShufflesUpToFiveCardsFromOwnGraveyard() {
        Permanent wand = addReadyWand(player1);
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card libraryCard = new Forest();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(libraryCard));
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        assertThat(wand.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore + 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wand.getCard());
    }

    @Test
    void cannotTargetCardsInOpponentsGraveyard() {
        addReadyWand(player1);
        Card opponentCard = new Forest();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroTargetsStillShufflesAndTriggersOpponentsTrickster() {
        Permanent wand = addReadyWand(player1);
        Permanent trickster = harness.addToBattlefieldAndReturn(player2, new CosisTrickster());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wand);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wand.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void shufflesFiveChosenCardsAndLeavesUnchosenCardInGraveyard() {
        addReadyWand(player1);
        List<Card> chosen = List.of(new Forest(), new Forest(), new Forest(), new GrizzlyBears(), new GrizzlyBears());
        Card unchosen = new Forest();
        harness.setGraveyard(player1, List.of(chosen.get(0), chosen.get(1), chosen.get(2),
                chosen.get(3), chosen.get(4), unchosen));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, chosen.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unchosen);
    }

    @Test
    void cannotChooseMoreThanFiveTargets() {
        Permanent wand = addReadyWand(player1);
        List<Card> cards = List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest());
        harness.setGraveyard(player1, cards);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, cards.stream().map(Card::getId).toList()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wand);
        assertThat(wand.isTapped()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(wand.getCard());
    }

    @Test
    void cannotChooseSameCardTwice() {
        addReadyWand(player1);
        Card card = new Forest();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(card.getId(), card.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void remainingLegalTargetIsShuffledWhenAnotherTargetLeavesGraveyard() {
        addReadyWand(player1);
        Card removed = new Forest();
        Card remaining = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(removed, remaining));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.setHand(player1, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(removed);
    }

    @Test
    void insufficientManaDoesNotExileOrTapWand() {
        Permanent wand = addReadyWand(player1);
        Card card = new Forest();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(card.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wand);
        assertThat(wand.isTapped()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(wand.getCard());
    }

    private Permanent addReadyWand(Player player) {
        return addCreatureReady(player, new WandOfVertebrae());
    }
}
