package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClingToDust.class, Forest.class, GrizzlyBears.class})
class ClingToDustTest extends BaseCardTest {

    @Test
    void exilingCreatureCardGainsThreeLife() {
        ClingToDust clingToDust = new ClingToDust();
        GrizzlyBears target = new GrizzlyBears();
        harness.setHand(player1, List.of(clingToDust));
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    void exilingNoncreatureCardDrawsACard() {
        ClingToDust clingToDust = new ClingToDust();
        Forest target = new Forest();
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of(clingToDust));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void escapeExilesFiveOtherCardsAndThenExilesClingToDustAfterResolution() {
        ClingToDust clingToDust = new ClingToDust();
        List<GrizzlyBears> otherCards = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        Forest target = new Forest();
        harness.setGraveyard(player1, List.of(clingToDust, otherCards.get(0), otherCards.get(1),
                otherCards.get(2), otherCards.get(3), otherCards.get(4), target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playFlashbackSpell(gd, player1, 0, null, target.getId(), List.of(), List.of(1, 2, 3, 4, 5));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(otherCards);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(clingToDust, target);
    }

    @Test
    void escapeRequiresFiveOtherCardsInTheGraveyard() {
        ClingToDust clingToDust = new ClingToDust();
        harness.setGraveyard(player1, List.of(clingToDust, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);
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
