package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeedlebiteTrap.class})
class NeedlebiteTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Target player loses 5 life and the controller gains 5 life")
    void drainsTargetPlayer() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new NeedlebiteTrap()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("May cast for {B} when an opponent gained life this turn")
    void castsForAlternateCostAfterOpponentGainedLife() {
        gd.lifeGainedThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new NeedlebiteTrap()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the alternate cost unless an opponent gained life")
    void alternateCostRequiresOpponentLifeGain() {
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new NeedlebiteTrap()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target yourself, losing and then gaining 5 life")
    void canTargetController() {
        harness.setHand(player1, List.of(new NeedlebiteTrap()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.lifeLostThisTurn.get(player1.getId())).isEqualTo(5);
        assertThat(gd.lifeGainedThisTurn.get(player1.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("Opponent life gain enables the alternate cost even without a net life increase")
    void actualOpponentLifeGainEnablesAlternateCost() {
        harness.setHand(player2, List.of(new NeedlebiteTrap()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.castAndResolveInstant(player2, 0, player2.getId());
        harness.assertLife(player2, 20);

        harness.setHand(player1, List.of(new NeedlebiteTrap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castWithAlternateCost(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The normal mana cost remains available after an opponent gains life")
    void canPayNormalCostWhenAlternateCostIsAvailable() {
        gd.lifeGainedThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new NeedlebiteTrap()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The alternate cost is unavailable when neither player gained life")
    void alternateCostRequiresLifeGainThisTurn() {
        harness.setHand(player1, List.of(new NeedlebiteTrap()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
