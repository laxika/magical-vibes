package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GargantuanLeech.class, Plains.class})
class GargantuanLeechTest extends BaseCardTest {

    @Test
    void costsFullAmountWithoutCaves() {
        harness.setHand(player1, List.of(new GargantuanLeech()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void costsOneLessForEachControlledCaveAndCaveCardInGraveyard() {
        harness.addToBattlefield(player1, cave());
        harness.setGraveyard(player1, List.of(cave()));
        harness.setHand(player1, List.of(new GargantuanLeech()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotCountOpponentsCavesOrNonCaveCardsInYourGraveyard() {
        harness.addToBattlefield(player2, cave());
        harness.setGraveyard(player1, List.of(new Plains()));
        harness.setHand(player1, List.of(new GargantuanLeech()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Card cave() {
        Card cave = new Plains().createRuntimeCopy();
        cave.setSubtypes(List.of(CardSubtype.CAVE));
        return cave;
    }
}
