package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollDiceEffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ClownCar.class)
class ClownCarTest extends BaseCardTest {

    private RollDiceEffectHandler rollDiceEffectHandler;
    private DiceRollService originalDiceRollService;

    @BeforeEach
    void captureDiceRollService() {
        rollDiceEffectHandler = GameTestEngineContext.get().getBean(RollDiceEffectHandler.class);
        originalDiceRollService = (DiceRollService) ReflectionTestUtils.getField(
                rollDiceEffectHandler, "diceRollService");
    }

    @AfterEach
    void restoreDiceRollService() {
        ReflectionTestUtils.setField(rollDiceEffectHandler, "diceRollService", originalDiceRollService);
    }

    @Test
    void oddResultsCreateClownRobotsAndEvenResultsAddCounters() {
        Permanent car = castClownCar(1, 2, 3, 4);

        assertThat(findPermanents(player1, "Clown Robot")).hasSize(2);
        assertThat(car.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void zeroRollsDoNothing() {
        Permanent car = castClownCar();

        assertThat(findPermanents(player1, "Clown Robot")).isEmpty();
        assertThat(car.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castClownCar(int... rolls) {
        ReflectionTestUtils.setField(rollDiceEffectHandler, "diceRollService", new FixedDiceRollService(rolls));
        ClownCar clownCar = new ClownCar();
        harness.setHand(player1, List.of(clownCar));
        harness.addMana(player1, ManaColor.COLORLESS, rolls.length);
        harness.castArtifact(player1, 0, rolls.length);
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == clownCar)
                .findFirst()
                .orElseThrow();
    }

    private static final class FixedDiceRollService extends DiceRollService {

        private final int[] results;
        private int index;

        private FixedDiceRollService(int... results) {
            this.results = results;
        }

        @Override
        public int roll(int sides) {
            return results[index++];
        }
    }
}
