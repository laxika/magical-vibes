package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.f.FireLordSozin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheRiseOfSozin.class, FireLordSozin.class, ChildOfNight.class,
        GrizzlyBears.class, HillGiant.class, Ornithopter.class})
class TheRiseOfSozinTest extends BaseCardTest {

    @Test
    void chapterIDestroysAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ChildOfNight());
        Permanent saga = addSaga(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Child of Night");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    void chapterIIExilesUpToFourNamedCardsFromTargetOpponentsZones() {
        Card graveyardCard = new GrizzlyBears();
        Card handCard = new GrizzlyBears();
        Card libraryCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setHand(player2, List.of(handCard));
        harness.setLibrary(player2, List.of(libraryCard));
        addSaga(1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1,
                List.of(graveyardCard.getId(), handCard.getId(), libraryCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(3);
    }

    @Test
    void chapterIIITransformsTheSaga() {
        addSaga(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent sozin = findPermanent(player1, "Fire Lord Sozin");
        assertThat(sozin).isNotNull();
        assertThat(sozin.isTransformed()).isTrue();
        harness.assertNotOnBattlefield(player1, "The Rise of Sozin");
    }

    @Test
    void fireLordSozinReturnsTargetCreaturesFromDamagedPlayersGraveyardWithinPaidX() {
        addTransformedSaga();
        Card bears = new GrizzlyBears();
        Card child = new ChildOfNight();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player2, List.of(bears, child, tooExpensive));
        gd.playerManaPools.get(player1.getId()).addPersistentMana(ManaColor.COLORLESS, 4);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 4);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(
                bears.getId(), child.getId(), tooExpensive.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), child.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Child of Night");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Child of Night");
    }

    @Test
    @DisplayName("Fire Lord Sozin offers only creature cards from the damaged player's graveyard, not its controller's")
    void fireLordSozinOffersOnlyTheDamagedPlayersGraveyard() {
        addTransformedSaga();
        Card opponentsBears = new GrizzlyBears();
        Card ownBears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentsBears));
        harness.setGraveyard(player1, List.of(ownBears));
        gd.playerManaPools.get(player1.getId()).addPersistentMana(ManaColor.COLORLESS, 4);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 4);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(opponentsBears.getId());
        harness.handleMultipleCardsChosen(player1, List.of(opponentsBears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("With three players, Fire Lord Sozin doesn't offer the graveyard of an opponent it didn't damage")
    void fireLordSozinDoesNotOfferAnUndamagedOpponentsGraveyard() {
        Player player3 = addThirdPlayer();
        addTransformedSaga();
        Card damagedPlayersBears = new GrizzlyBears();
        Card otherOpponentsBears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(damagedPlayersBears));
        harness.setGraveyard(player3, List.of(otherOpponentsBears));
        gd.playerManaPools.get(player1.getId()).addPersistentMana(ManaColor.COLORLESS, 4);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 4);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(damagedPlayersBears.getId());
    }

    @Test
    void chapterIIExilesOnlyFourOfFiveMatchingCards() {
        List<Card> cards = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player2, cards);
        addSaga(1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1,
                cards.subList(0, 4).stream().map(Card::getId).toList());

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrderElementsOf(cards.subList(0, 4));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(cards.get(4));
    }

    @Test
    void chapterIICanExileNoMatchingCards() {
        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));
        addSaga(1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIIReturnsAnOpponentsSagaUnderAbilityControllersControl() {
        TheRiseOfSozin opponentsSaga = new TheRiseOfSozin();
        opponentsSaga.setOwnerId(player2.getId());
        Permanent saga = harness.addToBattlefieldAndReturn(player1, opponentsSaga);
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fire Lord Sozin");
        harness.assertNotOnBattlefield(player2, "Fire Lord Sozin");
    }

    @Test
    void fireLordSozinCanPayZeroToReturnZeroManaValueCreatures() {
        addTransformedSaga();
        Card ornithopter = new Ornithopter();
        harness.setGraveyard(player2, List.of(ornithopter));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotInGraveyard(player2, "Ornithopter");
    }

    @Test
    void fireLordSozinCanUseFirebendingManaToReanimate() {
        addTransformedSaga();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 3);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining Fire Lord Sozin's payment returns nothing, even a mana value 0 creature card")
    void fireLordSozinDecliningReturnsNothing() {
        addTransformedSaga();
        Card ornithopter = new Ornithopter();
        harness.setGraveyard(player2, List.of(ornithopter));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheRiseOfSozin());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addTransformedSaga() {
        TheRiseOfSozin front = new TheRiseOfSozin();
        Permanent saga = harness.addToBattlefieldAndReturn(player1, front);
        saga.setCard(front.getBackFaceCard());
        saga.setTransformed(true);
        saga.setSummoningSick(false);
        return saga;
    }

    private Player addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
        return player3;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
