package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuelistsConvocationInternational.class, Forest.class})
class DuelistsConvocationInternationalTest extends BaseCardTest {

    @Test
    void entersWithTenRandomDigits() {
        Permanent source = addCard();

        assertThat(source.getChosenNumberDigits()).hasSize(10)
                .allMatch(digit -> digit >= 0 && digit <= 9);
        assertThat(source.getCrossedNumberDigitPositions()).isEmpty();
    }

    @Test
    void matchingLandPlayDrawsAndCrossesOneDigit() {
        Permanent source = addCard();
        source.setChosenNumberDigits(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(source.getCrossedNumberDigitPositions()).containsExactly(0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void matchingSpellDrawsAndCrossesOneDigit() {
        Permanent source = addCard();
        source.setChosenNumberDigits(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));

        Card spell = spellWithManaValue(4);
        harness.castFromHand(player1, spell, "{4}");
        harness.passBothPriorities();

        assertThat(source.getCrossedNumberDigitPositions()).containsExactly(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void crossingTheLastDigitWinsAfterDrawing() {
        Permanent source = addCard();
        source.setChosenNumberDigits(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        source.getCrossedNumberDigitPositions().addAll(IntStream.range(0, 9).boxed().toList());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, spellWithManaValue(9), "{9}");
        harness.passBothPriorities();

        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.WIN);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    private Permanent addCard() {
        harness.setHand(player1, List.of(new DuelistsConvocationInternational()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Duelists' Convocation International");
    }

    private static Card spellWithManaValue(int manaValue) {
        Card card = new Card();
        card.setName("Test Spell " + manaValue);
        card.setType(CardType.INSTANT);
        card.setManaCost("{" + manaValue + "}");
        return card;
    }
}
