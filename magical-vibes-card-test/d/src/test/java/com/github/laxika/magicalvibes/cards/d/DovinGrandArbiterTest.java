package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ConcordiaPegasus;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DovinGrandArbiter.class, ConcordiaPegasus.class})
class DovinGrandArbiterTest extends BaseCardTest {

    @Test
    @DisplayName("+1 adds a loyalty counter when a creature deals combat damage")
    void plusOneAddsLoyaltyAfterCombatDamage() {
        Permanent dovin = addReadyDovin(3);
        Permanent attacker = addCreatureReady(player1, new ConcordiaPegasus());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 expires at the end of the turn")
    void plusOneExpiresAtEndOfTurn() {
        Permanent dovin = addReadyDovin(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        advanceToNextTurn(player1);
        advanceToNextTurn(player2);

        Permanent attacker = addCreatureReady(player1, new ConcordiaPegasus());
        attacker.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-1 creates a Thopter and gains 1 life")
    void minusOneCreatesThopterAndGainsLife() {
        Permanent dovin = addReadyDovin(3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent thopter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(thopter.getCard().getName()).isEqualTo("Thopter");
        assertThat(thopter.getCard().getPower()).isEqualTo(1);
        assertThat(thopter.getCard().getToughness()).isEqualTo(1);
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-7 puts three of the top ten cards into hand and the rest on the bottom randomly")
    void minusSevenChoosesThreeOfTopTen() {
        List<Card> topCards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            topCards.add(new ConcordiaPegasus());
        }
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, topCards);
        addReadyDovin(7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.randomRemainingToBottom()).isTrue();
        assertThat(choice.reorderRemainingToBottom()).isFalse();
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.allCards()).containsExactlyElementsOf(topCards);

        List<Card> chosen = topCards.subList(0, 3);
        harness.handleMultipleCardsChosen(player1, chosen.stream().map(Card::getId).toList());

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(chosen);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards.subList(3, 10));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("+1 still counts the activating player's creatures after Dovin changes control")
    void plusOneKeepsOriginalControllerAfterControlChange() {
        Permanent dovin = addReadyDovin(3);
        Permanent attacker = addCreatureReady(player1, new ConcordiaPegasus());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(dovin);
        gd.playerBattlefields.get(player2.getId()).add(dovin);
        attacker.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 triggers separately for each creature dealing combat damage")
    void plusOneCountsEachCreature() {
        Permanent dovin = addReadyDovin(3);
        Permanent first = addCreatureReady(player1, new ConcordiaPegasus());
        Permanent second = addCreatureReady(player1, new ConcordiaPegasus());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        first.setAttacking(true);
        second.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(dovin.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-7 takes all cards when fewer than three remain")
    void minusSevenWithTwoCards() {
        List<Card> cards = List.of(new ConcordiaPegasus(), new ConcordiaPegasus());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, cards);
        addReadyDovin(8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(cards);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("-7 leaves cards below the top ten above the randomly bottomed cards")
    void minusSevenPreservesUnseenCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            cards.add(new ConcordiaPegasus());
        }
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, cards);
        addReadyDovin(8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        List<Card> chosen = List.of(cards.get(1), cards.get(4), cards.get(9));
        harness.handleMultipleCardsChosen(player1, chosen.stream().map(Card::getId).toList());

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(chosen);
        List<Card> remaining = gd.playerDecks.get(player1.getId());
        assertThat(remaining.subList(0, 2)).containsExactlyElementsOf(cards.subList(10, 12));
        assertThat(remaining.subList(2, remaining.size())).containsExactlyInAnyOrderElementsOf(
                cards.subList(0, 10).stream().filter(card -> !chosen.contains(card)).toList());
    }

    private Permanent addReadyDovin(int loyalty) {
        Permanent dovin = harness.addToBattlefieldAndReturn(player1, new DovinGrandArbiter());
        dovin.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return dovin;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
