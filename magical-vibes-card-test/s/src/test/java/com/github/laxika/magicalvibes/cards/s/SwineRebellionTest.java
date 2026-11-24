package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.ThayanEvokers;
import com.github.laxika.magicalvibes.cards.f.FirstLittlePig;
import com.github.laxika.magicalvibes.cards.t.ThirdLittlePig;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwineRebellion.class, FirstLittlePig.class, SecondLittlePig.class, ThirdLittlePig.class, ThayanEvokers.class})
class SwineRebellionTest extends BaseCardTest {

    @Test
    @DisplayName("Three Boars with different names conjure the whole spellbook onto the battlefield")
    void threeDistinctBoarsConjureTheWholeSpellbookOntoTheBattlefield() {
        harness.addToBattlefield(player1, new FirstLittlePig());
        harness.addToBattlefield(player1, new SecondLittlePig());
        harness.addToBattlefield(player1, new ThirdLittlePig());
        castSwineRebellion();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("First Little Pig", "Second Little Pig", "Third Little Pig",
                        "First Little Pig", "Second Little Pig", "Third Little Pig");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Otherwise conjures two chosen cards into hand and puts one of them onto the battlefield")
    void fewerThanThreeDistinctBoarsConjureTwoChoicesAndPutOneOntoTheBattlefield() {
        castSwineRebellion();

        PendingInteraction.SpellbookCardChoice spellbookChoice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        assertThat(spellbookChoice).isNotNull();
        assertThat(spellbookChoice.minCount()).isEqualTo(2);
        assertThat(spellbookChoice.maxCount()).isEqualTo(2);

        List<Card> chosenCards = spellbookChoice.cards().subList(0, 2);
        harness.handleMultipleCardsChosen(player1, chosenCards.stream().map(Card::getId).toList());

        PendingInteraction.MultiGraveyardChoice battlefieldChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(battlefieldChoice).isNotNull();
        assertThat(battlefieldChoice.minCount()).isEqualTo(1);
        chooseOfferedCards(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().getName())
                .isIn("First Little Pig", "Second Little Pig", "Third Little Pig");
    }

    @Test
    @DisplayName("Boars with the same name count once")
    void duplicateBoarsDoNotMeetTheDistinctNameThreshold() {
        harness.addToBattlefield(player1, new FirstLittlePig());
        harness.addToBattlefield(player1, new FirstLittlePig());
        castSwineRebellion();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Three Boars with two names take the hand branch")
    void threeBoarsWithOnlyTwoNamesUseTheHandBranch() {
        harness.addToBattlefield(player1, new FirstLittlePig());
        harness.addToBattlefield(player1, new FirstLittlePig());
        harness.addToBattlefield(player1, new SecondLittlePig());
        castSwineRebellion();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class))
                .isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An opponent's Boars don't count")
    void opponentsBoarsDoNotCountTowardsTheThreshold() {
        harness.addToBattlefield(player1, new FirstLittlePig());
        harness.addToBattlefield(player1, new SecondLittlePig());
        harness.addToBattlefield(player2, new ThirdLittlePig());
        castSwineRebellion();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class))
                .isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Only the two conjured cards are offered to put onto the battlefield")
    void battlefieldChoiceIsRestrictedToTheTwoNewlyConjuredCards() {
        harness.setHand(player1, List.of(new SwineRebellion(), new ThirdLittlePig()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.SpellbookCardChoice spellbookChoice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        List<Card> chosenCards = spellbookChoice.cards().stream()
                .filter(card -> !card.getName().equals("Third Little Pig"))
                .toList();
        harness.handleMultipleCardsChosen(player1, chosenCards.stream().map(Card::getId).toList());

        PendingInteraction.MultiGraveyardChoice battlefieldChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(battlefieldChoice.cards()).containsExactlyInAnyOrderElementsOf(chosenCards);
        String selectedName = battlefieldChoice.cards().getLast().getName();
        chooseOfferedCards(player1, 1);

        harness.assertOnBattlefield(player1, selectedName);
        harness.assertInHand(player1, "Third Little Pig");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    private void castSwineRebellion() {
        harness.setHand(player1, List.of(new SwineRebellion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Conjuring two cards into hand is one conjure event")
    void conjuringTwoCardsIntoHandTriggersConjureAbilitiesOnce() {
        Permanent evokers = harness.addToBattlefieldAndReturn(player1, new ThayanEvokers());
        castSwineRebellion();

        PendingInteraction.SpellbookCardChoice spellbookChoice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        harness.handleMultipleCardsChosen(player1, spellbookChoice.cards().subList(0, 2).stream()
                .map(Card::getId).toList());
        chooseOfferedCards(player1, 0);
        resolveAllTriggers();

        assertThat(evokers.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("The log names the conjured card put onto the battlefield, not the one left in hand")
    void logNamesNoConjuredCardLeftInHand() {
        castSwineRebellion();
        PendingInteraction.SpellbookCardChoice spellbookChoice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        Card first = spellbookChoice.cards().getFirst();
        Card second = spellbookChoice.cards().stream()
                .filter(card -> !card.getName().equals(first.getName())).findFirst().orElseThrow();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.gameLog).noneMatch(line -> line.plainText().contains(second.getName()));
        assertThat(gd.gameLog).anyMatch(line -> line.plainText().equals(
                player1.getUsername() + " conjures 2 cards from a spellbook into their hand."));
    }
}
