package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FirstLittlePig;
import com.github.laxika.magicalvibes.cards.t.ThirdLittlePig;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwineRebellion.class, FirstLittlePig.class, SecondLittlePig.class, ThirdLittlePig.class})
class SwineRebellionTest extends BaseCardTest {

    @Test
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
    void fewerThanThreeDistinctBoarsConjureTwoChoicesAndPutOneOntoTheBattlefield() {
        castSwineRebellion();

        PendingInteraction.SpellbookCardChoice spellbookChoice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        assertThat(spellbookChoice).isNotNull();
        assertThat(spellbookChoice.minCount()).isEqualTo(2);
        assertThat(spellbookChoice.maxCount()).isEqualTo(2);

        List<Card> chosenCards = spellbookChoice.cards().subList(0, 2);
        harness.handleMultipleCardsChosen(player1, chosenCards.stream().map(Card::getId).toList());

        PendingInteraction.RevealedHandChoice battlefieldChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(battlefieldChoice).isNotNull();
        harness.handleCardChosen(player1, battlefieldChoice.validIndices().getFirst());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().getName())
                .isIn("First Little Pig", "Second Little Pig", "Third Little Pig");
    }

    @Test
    void duplicateBoarsDoNotMeetTheDistinctNameThreshold() {
        harness.addToBattlefield(player1, new FirstLittlePig());
        harness.addToBattlefield(player1, new FirstLittlePig());
        castSwineRebellion();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class))
                .isNotNull();
    }

    @Test
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

        PendingInteraction.RevealedHandChoice battlefieldChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(battlefieldChoice.validIndices()).hasSize(2);
        assertThat(battlefieldChoice.validIndices())
                .allSatisfy(index -> assertThat(gd.playerHands.get(player1.getId()).get(index).getId())
                        .isIn(chosenCards.stream().map(Card::getId).toList()));
        int selectedIndex = battlefieldChoice.validIndices().getLast();
        String selectedName = gd.playerHands.get(player1.getId()).get(selectedIndex).getName();
        harness.handleCardChosen(player1, selectedIndex);

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
}
