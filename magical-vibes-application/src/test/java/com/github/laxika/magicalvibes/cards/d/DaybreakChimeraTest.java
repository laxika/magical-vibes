package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ElspethConquersDeath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaybreakChimera.class, ElspethConquersDeath.class})
class DaybreakChimeraTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for its full cost with no white devotion")
    void canBeCastForFullCostWithNoWhiteDevotion() {
        harness.castFromHand(player1, new DaybreakChimera(), "{W}{W}{W}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs two less for two white mana symbols among permanents you control")
    void costsTwoLessForTwoWhiteDevotion() {
        harness.addToBattlefield(player1, new ElspethConquersDeath());
        harness.castFromHand(player1, new DaybreakChimera(), "{W}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not count an opponent's white devotion")
    void doesNotCountOpponentsWhiteDevotion() {
        harness.addToBattlefield(player2, new ElspethConquersDeath());
        harness.setHand(player1, List.of(new DaybreakChimera()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Devotion greater than three reduces the cost to two white mana")
    void excessDevotionDoesNotReduceColoredCost() {
        harness.addToBattlefield(player1, new DaybreakChimera());
        harness.addToBattlefield(player1, new DaybreakChimera());

        harness.castFromHand(player1, new DaybreakChimera(), "{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("High devotion cannot replace a required white mana with colorless mana")
    void stillRequiresTwoWhiteManaWithHighDevotion() {
        harness.addToBattlefield(player1, new DaybreakChimera());
        harness.addToBattlefield(player1, new DaybreakChimera());
        harness.setHand(player1, List.of(new DaybreakChimera()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("White cards in hand and graveyard do not contribute devotion")
    void doesNotCountWhiteCardsOutsideBattlefield() {
        harness.setHand(player1, List.of(new DaybreakChimera(), new DaybreakChimera()));
        harness.setGraveyard(player1, List.of(new DaybreakChimera()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Tapped permanents still contribute white devotion")
    void tappedPermanentsStillContributeDevotion() {
        harness.addToBattlefieldAndReturn(player1, new DaybreakChimera()).setTapped(true);

        harness.castFromHand(player1, new DaybreakChimera(), "{W}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
