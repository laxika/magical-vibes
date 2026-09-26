package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RidersOfRohan.class)
class RidersOfRohanTest extends BaseCardTest {

    @Test
    void createsTwoHastyTramplingHumanKnights() {
        castNormally();

        List<Permanent> knights = findPermanents(player1, "Human Knight");
        assertThat(knights).hasSize(2);
        assertThat(knights).allSatisfy(knight -> {
            assertThat(knight.getCard().getPower()).isEqualTo(2);
            assertThat(knight.getCard().getToughness()).isEqualTo(2);
            assertThat(knight.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(knight.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.KNIGHT);
            assertThat(knight.getCard().getKeywords())
                    .contains(Keyword.TRAMPLE, Keyword.HASTE);
        });
    }

    @Test
    void normalCastDoesNotReturnAtEndStep() {
        castNormally();

        Permanent riders = findPermanent(player1, "Riders of Rohan");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Riders of Rohan")).isSameAs(riders);
    }

    @Test
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new RidersOfRohan()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent riders = findPermanent(player1, "Riders of Rohan");
        assertThat(riders.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(findPermanents(player1, "Human Knight")).hasSize(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Riders of Rohan");
        harness.assertNotOnBattlefield(player1, "Riders of Rohan");
        assertThat(findPermanents(player1, "Human Knight")).hasSize(2);
    }

    private void castNormally() {
        harness.setHand(player1, List.of(new RidersOfRohan()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
