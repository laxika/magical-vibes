package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ErebosGodOfTheDead;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyshroudCutter.class, Forest.class, ErebosGodOfTheDead.class})
class SkyshroudCutterTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for its alternate cost when you control a Forest")
    void castsForAlternateCost() {
        harness.addToBattlefield(player1, new Forest());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());
        harness.setHand(player1, List.of(new SkyshroudCutter()));

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife + 5);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyshroud Cutter");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife + 5);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the alternate cost without controlling a Forest")
    void alternateCostRequiresForest() {
        harness.setHand(player1, List.of(new SkyshroudCutter()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Forest does not enable the alternate cost")
    void opponentsForestDoesNotEnableAlternateCost() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SkyshroudCutter()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Skyshroud Cutter");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A tapped Forest enables the alternate cost and remains on the battlefield")
    void tappedForestEnablesAlternateCost() {
        var forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.setHand(player1, List.of(new SkyshroudCutter()));

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyshroud Cutter");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 25);
    }

    @Test
    @DisplayName("Is not playable without mana when another player cannot gain life")
    void unavailableWhenLifeGainCostCannotBePaid() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new ErebosGodOfTheDead());
        harness.setHand(player1, List.of(new SkyshroudCutter()));

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).doesNotContain(0);
    }

    @Test
    @DisplayName("Cannot use the alternate cost if an opponent cannot gain life")
    void alternateCostRequiresOpponentCanGainLife() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new ErebosGodOfTheDead());
        harness.setHand(player1, List.of(new SkyshroudCutter()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can pay the normal mana cost while controlling a Forest")
    void castsNormallyWhenAlternateCostIsAvailable() {
        harness.addToBattlefield(player1, new Forest());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new SkyshroudCutter(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyshroud Cutter");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Can be cast normally for its mana cost")
    void castsNormally() {
        harness.castFromHand(player1, new SkyshroudCutter(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyshroud Cutter");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
