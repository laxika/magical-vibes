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

    @Test
    @DisplayName("Checks opponent life loss when entering, even if it happened after casting")
    void lifeLossWhileSpellIsOnStackQualifies() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FrilledSparkshooter()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);

        harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "Test life loss");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Frilled Sparkshooter")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life gain does not undo an opponent's earlier life loss")
    void lifeGainAfterLifeLossStillQualifies() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 2, "Test life loss");
        harness.getLifeSupport().applyGainLife(gd, player2.getId(), 5);

        castSparkshooter();

        assertThat(findPermanent(player1, "Frilled Sparkshooter")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters with only one counter regardless of how much life the opponent lost")
    void largerLifeLossStillGivesOneCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 5, "Test life loss");

        castSparkshooter();

        assertThat(findPermanent(player1, "Frilled Sparkshooter")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent life loss after entry does not add a counter retroactively")
    void lifeLossAfterEntryDoesNotAddCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castSparkshooter();

        harness.getLifeSupport().applyLifeLoss(gd, player2.getId(), 1, "Test life loss");

        assertThat(findPermanent(player1, "Frilled Sparkshooter")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void castSparkshooter() {
        harness.setHand(player1, List.of(new FrilledSparkshooter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
