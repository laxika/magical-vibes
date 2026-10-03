package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mintstrosity;
import com.github.laxika.magicalvibes.cards.n.NotDeadAfterAll;
import com.github.laxika.magicalvibes.cards.y.YawgmothsBargain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshiokWickedManipulator.class, Forest.class, Mintstrosity.class, NotDeadAfterAll.class,
        YawgmothsBargain.class})
class AshiokWickedManipulatorTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts one of the top two cards into hand and exiles the other")
    void plusOneChoosesOneCardForHandAndExilesTheOther() {
        Permanent ashiok = addReadyAshiok(player1, 3);
        Card exiled = new Forest();
        Card toHand = new Mintstrosity();
        harness.setLibrary(player1, List.of(exiled, toHand));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(toHand.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(toHand);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
    }

    @Test
    @DisplayName("-2 creates Nightmare tokens that get counters at combat after an exile")
    void minusTwoCreatesNightmaresThatGetCountersAtBeginningOfCombat() {
        Permanent ashiok = addReadyAshiok(player1, 3);
        gd.addToExile(player1.getId(), new NotDeadAfterAll());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> nightmares = findPermanents(player1, "Nightmare");
        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(nightmares).hasSize(2);
        assertThat(nightmares).allSatisfy(token ->
                assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(nightmares).allSatisfy(token ->
                assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("-7 exiles as many cards as the mana value of owned exiled cards")
    void minusSevenExilesCardsBasedOnOwnedExile() {
        addReadyAshiok(player1, 7);
        Card ownedExile = new Mintstrosity();
        gd.addToExile(player1.getId(), ownedExile);
        Card first = new Forest();
        Card second = new NotDeadAfterAll();
        Card third = new Forest();
        harness.setLibrary(player2, List.of(first, second, third));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Ashiok replaces a life payment with exiling from the library")
    void replacesLifePaymentWithExile() {
        addReadyAshiok(player1, 3);
        harness.addToBattlefield(player1, new YawgmothsBargain());
        Card paidInstead = new Forest();
        Card drawn = new Mintstrosity();
        harness.setLibrary(player1, List.of(paidInstead, drawn));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(paidInstead);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void plusOneExilesTheOnlyCardInLibrary() {
        addReadyAshiok(player1, 5);
        Card onlyCard = new Mintstrosity();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void plusOneWithEmptyLibraryDoesNotDrawOrLoseTheGame() {
        Permanent ashiok = addReadyAshiok(player1, 5);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ashiok.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusSevenIgnoresFaceDownExiledCardsAndOpponentsCards() {
        addReadyAshiok(player1, 7);
        gd.addToExile(player1.getId(), new Mintstrosity(), null, true);
        gd.addToExile(player2.getId(), new Mintstrosity());
        Card first = new Forest();
        Card second = new NotDeadAfterAll();
        harness.setLibrary(player2, List.of(first, second));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, second);
    }

    @Test
    void minusSevenUsesExileTotalAtResolutionAndCanTargetItsController() {
        addReadyAshiok(player1, 7);
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        gd.addToExile(player1.getId(), new Mintstrosity());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
    }

    @Test
    void minusSevenExilesEntireLibraryWhenTotalExceedsItsSize() {
        addReadyAshiok(player1, 7);
        gd.addToExile(player1.getId(), new Mintstrosity());
        Card onlyCard = new Forest();
        harness.setLibrary(player2, List.of(onlyCard));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(onlyCard);
    }

    @Test
    void nightmaresDoNotTriggerWithoutAnExileBeforeCombat() {
        addReadyAshiok(player1, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        gd.addToExile(player1.getId(), new NotDeadAfterAll());
        assertThat(findPermanents(player1, "Nightmare")).allSatisfy(token ->
                assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void nightmaresTriggerForAnOpponentsExileEvenAfterAshiokLeaves() {
        Permanent ashiok = addReadyAshiok(player1, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(ashiok);
        gd.addToExile(player2.getId(), new Forest());

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Nightmare")).hasSize(2).allSatisfy(token ->
                assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    void nightmaresDoNotTriggerOnOpponentsTurn() {
        addReadyAshiok(player1, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        gd.addToExile(player1.getId(), new Forest());
        harness.forceActivePlayer(player2);

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Nightmare")).allSatisfy(token ->
                assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void paysLifeNormallyWhenLibraryCannotSupplyReplacement() {
        addReadyAshiok(player1, 5);
        harness.addToBattlefield(player1, new YawgmothsBargain());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentsLifePaymentsAreNotReplaced() {
        addReadyAshiok(player1, 5);
        harness.addToBattlefield(player2, new YawgmothsBargain());
        Card drawn = new Forest();
        harness.setLibrary(player2, List.of(drawn));
        harness.setLife(player2, 20);
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).contains(drawn);
    }

    private Permanent addReadyAshiok(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AshiokWickedManipulator());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
