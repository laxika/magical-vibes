package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EverflowingChalice;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.w.WalkingBallista;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Vault21HouseGambit.class, GrizzlyBears.class, HillGiant.class, Mountain.class, Opt.class,
        WalkingBallista.class, EverflowingChalice.class})
class Vault21HouseGambitTest extends BaseCardTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void chaptersIAndIIDiscardThenDraw(int loreCounters) {
        Card discarded = new GrizzlyBears();
        Card drawn = new HillGiant();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        addSaga(loreCounters);

        triggerChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void chaptersIAndIIDrawEvenWithAnEmptyHand(int loreCounters) {
        Card drawn = new Vault21HouseGambit();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        addSaga(loreCounters);

        triggerChapter();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void enteringBattlefieldTriggersChapterI() {
        Card discarded = new Vault21HouseGambit();
        Card drawn = new Vault21HouseGambit();
        harness.setLibrary(player1, List.of(drawn));

        harness.castFromHand(player1, new Vault21HouseGambit(), "{1}{R}");
        harness.setHand(player1, List.of(discarded));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        Permanent saga = findPermanent(player1, "Vault 21: House Gambit");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    void chapterIIICanChooseCardsBeyondTheFirstFiveNonlands() {
        List<Card> cards = List.of(new Vault21HouseGambit(), new Vault21HouseGambit(),
                new Vault21HouseGambit(), new Vault21HouseGambit(),
                new Vault21HouseGambit(), new Vault21HouseGambit());
        Permanent saga = addSaga(2);
        harness.setHand(player1, cards);

        triggerChapter();
        harness.handleMultipleCardsChosen(player1,
                List.of(cards.get(4).getId(), cards.get(5).getId()));

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(cards);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void chapterIIICreatesOneTreasurePerCardRatherThanPerPair() {
        List<Card> cards = List.of(new Vault21HouseGambit(), new Vault21HouseGambit(),
                new Vault21HouseGambit());
        addSaga(2);
        harness.setHand(player1, cards);

        triggerChapter();
        harness.handleMultipleCardsChosen(player1, cards.stream().map(Card::getId).toList());

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(cards);
    }

    @Test
    void chapterIIICannotRevealMoreThanFiveCards() {
        List<Card> cards = List.of(new Vault21HouseGambit(), new Vault21HouseGambit(),
                new Vault21HouseGambit(), new Vault21HouseGambit(),
                new Vault21HouseGambit(), new Vault21HouseGambit());
        addSaga(2);
        harness.setHand(player1, cards);

        triggerChapter();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                cards.stream().map(Card::getId).toList())).isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(cards);
        harness.handleMultipleCardsChosen(player1, List.of());
    }

    @Test
    void chapterIIIMayRevealNoCards() {
        Card card = new Vault21HouseGambit();
        Permanent saga = addSaga(2);
        harness.setHand(player1, List.of(card));

        triggerChapter();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void chapterIIIRevealingOnlyOneCardCreatesNoTreasure() {
        Card selected = new Vault21HouseGambit();
        Card unrevealed = new Vault21HouseGambit();
        addSaga(2);
        harness.setHand(player1, List.of(selected, unrevealed));

        triggerChapter();
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected, unrevealed);
    }

    @Test
    void chapterIIIWithOnlyLandsCreatesNoTreasureAndSacrificesSaga() {
        Card land = new Mountain();
        Permanent saga = addSaga(2);
        harness.setHand(player1, List.of(land));

        triggerChapter();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void chapterIIITreatsXInHandAsZeroForManaValue() {
        Card ballista = new WalkingBallista();
        Card chalice = new EverflowingChalice();
        addSaga(2);
        harness.setHand(player1, List.of(ballista, chalice));

        triggerChapter();
        harness.handleMultipleCardsChosen(player1, List.of(ballista.getId(), chalice.getId()));

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ballista, chalice);
    }

    @Test
    void chapterIIIRevealsUpToFiveNonlandsAndCreatesTreasureForEachDuplicate() {
        Card bearsA = new GrizzlyBears();
        Card bearsB = new GrizzlyBears();
        Card giantsA = new HillGiant();
        Card giantsB = new HillGiant();
        Card unique = new Opt();
        Card land = new Mountain();
        Permanent saga = addSaga(2);
        harness.setHand(player1, List.of(bearsA, bearsB, giantsA, giantsB, unique, land));

        triggerChapter();

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice.validCardIds()).containsExactly(
                bearsA.getId(), bearsB.getId(), giantsA.getId(), giantsB.getId(), unique.getId());
        harness.handleMultipleCardsChosen(player1,
                List.of(bearsA.getId(), bearsB.getId(), giantsA.getId(), giantsB.getId(), unique.getId()));

        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(bearsA, bearsB, giantsA, giantsB, unique, land);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new Vault21HouseGambit());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
