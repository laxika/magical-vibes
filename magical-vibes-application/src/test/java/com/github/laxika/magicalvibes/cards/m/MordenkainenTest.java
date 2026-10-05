package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({Mordenkainen.class, Forest.class, Island.class})
class MordenkainenTest extends BaseCardTest {

    @Test
    @DisplayName("+2 draws two cards and puts one on the bottom")
    void plusTwoDrawsAndBottomsOne() {
        Permanent mordenkainen = addReadyMordenkainen(player1, 5);
        Card kept = new Forest();
        Card firstDraw = new Forest();
        Card secondDraw = new Island();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(mordenkainen.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(firstDraw.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw);
    }

    @Test
    @DisplayName("-2 creates a Dog Illusion whose power and toughness track hand size")
    void minusTwoCreatesHandSizeToken() {
        addReadyMordenkainen(player1, 2);
        Card first = new Forest();
        Card second = new Island();
        Card third = new Forest();
        harness.setHand(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent dog = findPermanent(player1, "Dog Illusion");
        assertThat(gqs.getEffectivePower(gd, dog)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, dog)).isEqualTo(6);

        harness.setHand(player1, List.of(first));

        assertThat(gqs.getEffectivePower(gd, dog)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dog)).isEqualTo(2);
    }

    @Test
    @DisplayName("-10 exchanges hand and library, shuffles, and grants no maximum hand size")
    void minusTenExchangesHandAndLibrary() {
        Permanent mordenkainen = addReadyMordenkainen(player1, 10);
        Card handCard = new Forest();
        Card libraryCard = new Forest();
        Card secondLibraryCard = new Island();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(libraryCard, secondLibraryCard));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(mordenkainen.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard, secondLibraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(handCard);
        assertThat(gd.playersWithNoMaximumHandSize).contains(player1.getId());
    }

    @Test
    @DisplayName("+2 can bottom an original hand card beneath the undrawn library")
    void plusTwoBottomsBelowRemainingLibrary() {
        addReadyMordenkainen(player1, 5);
        Card original = new Forest();
        Card firstDraw = new Island();
        Card secondDraw = new Forest();
        Card undrawn = new Island();
        harness.setHand(player1, List.of(original));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, undrawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(original.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn, original);
    }

    @Test
    @DisplayName("-2's Dog dies when its controller has no cards in hand")
    void minusTwoWithEmptyHandCreatesTokenThatDies() {
        addReadyMordenkainen(player1, 5);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest(), new Island()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Dog Illusion"));
    }

    @Test
    @DisplayName("-10 exchanges an empty hand without drawing or changing the opponent's zones")
    void minusTenExchangesEmptyHand() {
        addReadyMordenkainen(player1, 10);
        Card first = new Forest();
        Card second = new Island();
        Card opponentHand = new Island();
        Card opponentLibrary = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player2, List.of(opponentHand));
        harness.setLibrary(player2, List.of(opponentLibrary));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentHand);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibrary);
    }

    @Test
    @DisplayName("-10 gives its controller an emblem even when Mordenkainen dies paying the cost")
    void minusTenCreatesEmblem() {
        addReadyMordenkainen(player1, 10);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Mordenkainen);
    }

    private Permanent addReadyMordenkainen(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Mordenkainen());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
