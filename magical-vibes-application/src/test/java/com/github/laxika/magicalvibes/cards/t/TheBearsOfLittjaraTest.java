package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TheBearsOfLittjaraTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates a blue 2/2 Shapeshifter with changeling")
    void chapterICreatesShapeshifter() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Shapeshifter"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().hasKeyword(Keyword.CHANGELING)).isTrue();
    }

    @Test
    @DisplayName("Chapter II sets any number of your Shapeshifters to 4/4 indefinitely")
    void chapterIISetsChosenShapeshiftersIndefinitely() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        Permanent first = harness.addToBattlefieldAndReturn(player1, shapeshifter("First", 1, 1));
        Permanent second = harness.addToBattlefieldAndReturn(player1, shapeshifter("Second", 2, 3));
        Permanent other = harness.addToBattlefieldAndReturn(player1, creature("Other", 6, 6));
        saga.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(first.getId(), second.getId()).doesNotContain(other.getId());

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("Chapter III lets creatures with power 4 or greater damage a creature or planeswalker")
    void chapterIIIDamagesPlaneswalkerWithLargeCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBearsOfLittjara());
        harness.addToBattlefield(player1, creature("Large Creature", 6, 6));
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, planeswalker(8));
        planeswalker.setCounterCount(CounterType.LOYALTY, 8);
        saga.setCounterCount(CounterType.LORE, 2);

        advanceToNextChapter();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(planeswalker.getId());

        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private static Card shapeshifter(String name, int power, int toughness) {
        Card card = creature(name, power, toughness);
        card.setSubtypes(List.of(CardSubtype.SHAPESHIFTER));
        card.setKeywords(Set.of(Keyword.CHANGELING));
        return card;
    }

    private static Card creature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card planeswalker(int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        return card;
    }
}
