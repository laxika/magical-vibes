package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpikewheelAcrobat.class, SkewerTheCritics.class})
class SpikewheelAcrobatTest extends BaseCardTest {

    @Test
    @DisplayName("Spectacle casts Spikewheel Acrobat for {2}{R} after an opponent loses life")
    void spectacleUsesAlternateCost() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new SpikewheelAcrobat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player1, "Spikewheel Acrobat");
    }

    @Test
    @DisplayName("Spectacle is unavailable when no opponent has lost life this turn")
    void spectacleRequiresOpponentLifeLoss() {
        harness.setHand(player1, List.of(new SpikewheelAcrobat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPayNormalCostWithoutOpponentLifeLoss() {
        harness.castFromHand(player1, new SpikewheelAcrobat(), "{3}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spikewheel Acrobat");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void controllersLifeLossDoesNotEnableSpectacle() {
        gd.lifeLostThisTurn.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new SpikewheelAcrobat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void spectacleStillRequiresFullAlternateManaCost() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new SpikewheelAcrobat()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageToOpponentEnablesSpectacleEvenAfterLifeIsRegained() {
        harness.setHand(player1, List.of(new SkewerTheCritics(), new SpikewheelAcrobat()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.assertLife(player2, 17);
        harness.setLife(player2, 20);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spikewheel Acrobat");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
