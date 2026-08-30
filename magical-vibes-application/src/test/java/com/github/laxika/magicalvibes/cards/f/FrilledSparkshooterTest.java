package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrilledSparkshooter.class})
class FrilledSparkshooterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter when an opponent lost life this turn")
    void entersWithCounterAfterOpponentLostLife() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        castSparkshooter();

        Permanent shooter = findPermanent(player1, "Frilled Sparkshooter");

        assertThat(shooter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(shooter.getEffectivePower()).isEqualTo(4);
        assertThat(shooter.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not enter with a +1/+1 counter when no opponent lost life this turn")
    void entersWithoutCounterWhenNoOpponentLostLife() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castSparkshooter();

        Permanent shooter = findPermanent(player1, "Frilled Sparkshooter");

        assertThat(shooter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(shooter.getEffectivePower()).isEqualTo(3);
        assertThat(shooter.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not qualify when only its controller lost life")
    void controllerLifeLossDoesNotQualify() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        castSparkshooter();

        Permanent shooter = findPermanent(player1, "Frilled Sparkshooter");

        assertThat(shooter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castSparkshooter() {
        harness.setHand(player1, List.of(new FrilledSparkshooter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
