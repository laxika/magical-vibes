package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FelhideBrawler;
import com.github.laxika.magicalvibes.cards.h.HeraldOfTorment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarshmistTitan.class, FelhideBrawler.class, HeraldOfTorment.class})
class MarshmistTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for its full cost with no black devotion")
    void canBeCastForFullCostWithNoBlackDevotion() {
        harness.castFromHand(player1, new MarshmistTitan(), "{6}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs one less for one black mana symbol among permanents you control")
    void costsOneLessForOneBlackDevotion() {
        harness.addToBattlefield(player1, new FelhideBrawler());
        harness.castFromHand(player1, new MarshmistTitan(), "{5}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not count an opponent's black devotion")
    void doesNotCountOpponentsBlackDevotion() {
        harness.addToBattlefield(player2, new FelhideBrawler());
        assertThatThrownBy(() -> harness.castFromHand(player1, new MarshmistTitan(), "{5}{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Counts each black symbol, including multiple symbols on one permanent")
    void countsMultipleBlackSymbolsOnOnePermanent() {
        harness.addToBattlefield(player1, new HeraldOfTorment());
        harness.castFromHand(player1, new MarshmistTitan(), "{4}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Devotion greater than six reduces the generic cost to zero")
    void excessDevotionReducesGenericCostToZero() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new HeraldOfTorment());
        }
        harness.castFromHand(player1, new MarshmistTitan(), "{B}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Marshmist Titan");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Excess devotion cannot replace the required black mana")
    void excessDevotionDoesNotReduceBlackCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new HeraldOfTorment());
        }

        assertThatThrownBy(() -> harness.castFromHand(player1, new MarshmistTitan(), "{1}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Black cards in hand and graveyard do not contribute devotion")
    void cardsOutsideBattlefieldDoNotContributeDevotion() {
        harness.setGraveyard(player1, List.of(new HeraldOfTorment()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player1, List.of(new MarshmistTitan(), new HeraldOfTorment()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
