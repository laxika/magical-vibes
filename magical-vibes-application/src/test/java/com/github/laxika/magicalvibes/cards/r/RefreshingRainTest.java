package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RefreshingRain.class, Forest.class, Swamp.class})
class RefreshingRainTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for free when you control a Forest and an opponent controls a Swamp")
    void castsForFreeWithRequiredLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new RefreshingRain()));
        int before = gd.playerLifeTotals.get(player2.getId());

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(before + 6);
    }

    @Test
    @DisplayName("Cannot be cast for free without the required land condition")
    void cannotCastForFreeWithoutRequiredLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new RefreshingRain()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast normally when the free-cast condition is not met")
    void castsNormallyWithoutRequiredLands() {
        harness.setHand(player1, List.of(new RefreshingRain()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int before = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(before + 6);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the free cost without a Forest you control")
    void alternateCostRequiresControllerForest() {
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new RefreshingRain()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not use a Swamp controlled by the spell's controller for the free cost")
    void alternateCostRequiresOpponentSwamp() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new RefreshingRain()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The free alternate cost does not spend mana")
    void alternateCostDoesNotSpendMana() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new RefreshingRain()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetController() {
        harness.setHand(player1, List.of(new RefreshingRain()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int before = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(before + 6);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        var forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RefreshingRain()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
