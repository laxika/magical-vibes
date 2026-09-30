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

        harness.assertOnBattlefield(player1, "First Little Pig");
        harness.assertOnBattlefield(player1, "Second Little Pig");
        harness.assertOnBattlefield(player1, "Third Little Pig");
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

    private void castSwineRebellion() {
        harness.setHand(player1, List.of(new SwineRebellion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
