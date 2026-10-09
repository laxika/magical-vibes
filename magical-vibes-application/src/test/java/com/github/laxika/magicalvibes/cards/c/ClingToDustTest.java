package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClingToDust.class, Forest.class, NyxbornCourser.class})
class ClingToDustTest extends BaseCardTest {

    @Test
    void exilingCreatureCardGainsThreeLife() {
        ClingToDust clingToDust = new ClingToDust();
        NyxbornCourser target = new NyxbornCourser();
        harness.setHand(player1, List.of(clingToDust));
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    void exilingNoncreatureCardDrawsACard() {
        ClingToDust clingToDust = new ClingToDust();
        Forest target = new Forest();
        NyxbornCourser drawnCard = new NyxbornCourser();
        harness.setHand(player1, List.of(clingToDust));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void escapeExilesFiveOtherCardsAndReturnsClingToDustToGraveyard() {
        harness.setHand(player1, List.of());
        ClingToDust clingToDust = new ClingToDust();
        List<NyxbornCourser> otherCards = List.of(
                new NyxbornCourser(), new NyxbornCourser(), new NyxbornCourser(),
                new NyxbornCourser(), new NyxbornCourser());
        Forest target = new Forest();
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setGraveyard(player1, List.of(clingToDust, otherCards.get(0), otherCards.get(1),
                otherCards.get(2), otherCards.get(3), otherCards.get(4), target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playFlashbackSpell(gd, player1, 0, null, target.getId(), List.of(), List.of(1, 2, 3, 4, 5));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(otherCards);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target).doesNotContain(clingToDust);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(clingToDust);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void creatureTargetLeavingGraveyardBeforeResolutionGivesNoBonus() {
        ClingToDust first = new ClingToDust();
        ClingToDust response = new ClingToDust();
        NyxbornCourser target = new NyxbornCourser();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(first));
        harness.setHand(player2, List.of(response));
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
    }

    @Test
    void escapeCanTargetOneOfTheCardsExiledToPayItsCost() {
        harness.setHand(player1, List.of());
        ClingToDust clingToDust = new ClingToDust();
        Forest target = new Forest();
        Forest drawnCard = new Forest();
        harness.setGraveyard(player1, List.of(clingToDust, target,
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        gs.playFlashbackSpell(gd, player1, 0, null, target.getId(), List.of(), List.of(1, 2, 3, 4, 5));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(5).contains(target);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(clingToDust);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void canEscapeAgainAfterResolving() {
        ClingToDust clingToDust = new ClingToDust();
        NyxbornCourser firstTarget = new NyxbornCourser();
        NyxbornCourser secondTarget = new NyxbornCourser();
        harness.setGraveyard(player1, List.of(clingToDust,
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player2, List.of(firstTarget, secondTarget));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        gs.playFlashbackSpell(gd, player1, 0, null, firstTarget.getId(), List.of(), List.of(1, 2, 3, 4, 5));
        harness.passBothPriorities();
        int cardIndex = gd.playerGraveyards.get(player1.getId()).indexOf(clingToDust);
        assertThat(cardIndex).isNotNegative();
        gs.playFlashbackSpell(gd, player1, cardIndex, null, secondTarget.getId(), List.of(),
                List.of(0, 1, 2, 3, 4));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(clingToDust);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(10).doesNotContain(clingToDust);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(firstTarget, secondTarget);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 6);
    }

    @Test
    void escapeCannotExileItselfAsOneOfTheFiveOtherCards() {
        ClingToDust clingToDust = new ClingToDust();
        NyxbornCourser target = new NyxbornCourser();
        harness.setGraveyard(player1, List.of(clingToDust,
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, target.getId(),
                List.of(), List.of(0, 1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(clingToDust).hasSize(6);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void escapeRequiresFiveOtherCardsInTheGraveyard() {
        ClingToDust clingToDust = new ClingToDust();
        NyxbornCourser target = new NyxbornCourser();
        harness.setGraveyard(player1, List.of(clingToDust, new NyxbornCourser(), new NyxbornCourser(),
                new NyxbornCourser(), new NyxbornCourser()));
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playFlashbackSpell(gd, player1, 0, null, target.getId(),
                List.of(), List.of(1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilingOwnEnchantmentCreatureGainsLifeWithoutDrawing() {
        ClingToDust clingToDust = new ClingToDust();
        NyxbornCourser target = new NyxbornCourser();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(clingToDust));
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(clingToDust);
    }

    @Test
    void cannotTargetCardOutsideAGraveyard() {
        ClingToDust clingToDust = new ClingToDust();
        Forest target = new Forest();
        harness.setHand(player1, List.of(clingToDust));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
