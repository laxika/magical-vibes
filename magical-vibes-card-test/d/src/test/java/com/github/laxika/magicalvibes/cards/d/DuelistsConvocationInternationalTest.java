package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.e.ErraticPortal;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.w.WalkingBallista;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuelistsConvocationInternational.class, DarksteelCitadel.class, Counterspell.class,
        ErraticPortal.class, Memnite.class, WalkingBallista.class})
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
        DarksteelCitadel drawnCard = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DarksteelCitadel()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(source.getCrossedNumberDigitPositions()).containsExactly(0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void matchingSpellDrawsAndCrossesOneDigit() {
        Permanent source = addCard();
        source.setChosenNumberDigits(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        DarksteelCitadel drawnCard = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(drawnCard));

        Card spell = new ErraticPortal();
        harness.castFromHand(player1, spell, "{4}");
        harness.passBothPriorities();

        assertThat(source.getCrossedNumberDigitPositions()).containsExactly(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void crossingTheLastDigitWinsAfterDrawing() {
        Permanent source = addCard();
        source.setChosenNumberDigits(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        source.getCrossedNumberDigitPositions().addAll(IntStream.range(1, 10).boxed().toList());
        harness.setLibrary(player1, List.of(new DarksteelCitadel()));

        harness.castFromHand(player1, new Memnite(), "{0}");
        harness.passBothPriorities();

        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.WIN);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void nonmatchingSpellDoesNotTrigger() {
        Permanent source = addCard();
        source.setChosenNumberDigits(List.of(1, 1, 1, 1, 1, 1, 1, 1, 1, 1));

        harness.castFromHand(player1, new Memnite(), "{0}");

        assertThat(gd.stack).hasSize(1);
        assertThat(source.getCrossedNumberDigitPositions()).isEmpty();
    }

    @Test
    void nonmatchingLandDoesNotTrigger() {
        Permanent source = addCard();
        source.setChosenNumberDigits(List.of(1, 1, 1, 1, 1, 1, 1, 1, 1, 1));
        harness.setHand(player1, List.of(new DarksteelCitadel()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(source.getCrossedNumberDigitPositions()).isEmpty();
    }

    @Test
    void repeatedDigitsCrossOnlyOncePerSpell() {
        Permanent source = addCard();
        source.setChosenNumberDigits(List.of(0, 0, 1, 1, 1, 1, 1, 1, 1, 1));
        DarksteelCitadel firstDraw = new DarksteelCitadel();
        DarksteelCitadel secondDraw = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.castFromHand(player1, new Memnite(), "{0}");
        harness.passBothPriorities();

        assertThat(source.getCrossedNumberDigitPositions()).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        harness.passBothPriorities();

        harness.castFromHand(player1, new Memnite(), "{0}");
        harness.passBothPriorities();

        assertThat(source.getCrossedNumberDigitPositions()).containsExactlyInAnyOrder(0, 1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDraw);
        harness.passBothPriorities();

        harness.castFromHand(player1, new Memnite(), "{0}");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentsSpellDoesNotTrigger() {
        Permanent source = addCard();
        source.setChosenNumberDigits(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Memnite(), "{0}");

        assertThat(gd.stack).hasSize(1);
        assertThat(source.getCrossedNumberDigitPositions()).isEmpty();
    }

    @Test
    void xSpellUsesBothXSymbolsInItsManaValue() {
        Permanent source = addCard();
        source.setChosenNumberDigits(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        DarksteelCitadel drawnCard = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new WalkingBallista()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(source.getCrossedNumberDigitPositions()).containsExactly(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void counteredXSpellRetainsItsManaValueForTheTrigger() {
        Permanent source = addCard();
        source.setChosenNumberDigits(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
        DarksteelCitadel drawnCard = new DarksteelCitadel();
        harness.setLibrary(player1, List.of(drawnCard));
        WalkingBallista spell = new WalkingBallista();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0, 2);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(source.getCrossedNumberDigitPositions()).containsExactly(4);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    private Permanent addCard() {
        harness.castFromHand(player1, new DuelistsConvocationInternational(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Duelists' Convocation International");
    }
}
