package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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

@CardUsed({ClownCar.class})
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

    @Test
    void allOddResultsCreateUntappedWhiteArtifactCreatures() {
        Permanent car = castClownCar(1, 3, 5);

        assertThat(car.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player2, "Clown Robot")).isEmpty();
        assertThat(findPermanents(player1, "Clown Robot")).hasSize(3).allSatisfy(robot -> {
            assertThat(robot.getCard().isToken()).isTrue();
            assertThat(gqs.isCreature(gd, robot)).isTrue();
            assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(robot.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(robot.getCard().getSubtypes()).contains(CardSubtype.CLOWN, CardSubtype.ROBOT);
            assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(1);
            assertThat(robot.isTapped()).isFalse();
        });
    }

    @Test
    void allEvenResultsAddCountersWithoutCreatingTokens() {
        Permanent car = castClownCar(2, 4, 6);

        assertThat(car.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(findPermanents(player1, "Clown Robot")).isEmpty();
        assertThat(gqs.isCreature(gd, car)).isFalse();
    }

    @Test
    void enteringWithoutBeingCastRollsNoDice() {
        ReflectionTestUtils.setField(rollDiceEffectHandler, "diceRollService", new FixedDiceRollService());
        Permanent car = harness.enterBattlefieldAndReturn(player1, new ClownCar());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clown Robot")).isEmpty();
        assertThat(car.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void freshlyCreatedRobotsCanCrewCarUntilEndOfTurn() {
        Permanent car = castClownCar(1, 3, 2);
        List<Permanent> robots = findPermanents(player1, "Clown Robot");
        assertThat(gqs.isCreature(gd, car)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(robots).hasSize(2).allSatisfy(robot -> assertThat(robot.isTapped()).isTrue());
        assertThat(gqs.isCreature(gd, car)).isTrue();
        assertThat(gqs.getEffectivePower(gd, car)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, car)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, car)).isFalse();
        assertThat(car.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castClownCar(int... rolls) {
        ReflectionTestUtils.setField(rollDiceEffectHandler, "diceRollService", new FixedDiceRollService(rolls));
        ClownCar clownCar = new ClownCar();
        harness.setHand(player1, List.of(clownCar));
        harness.addMana(player1, ManaColor.COLORLESS, rolls.length);
        harness.castArtifact(player1, 0, rolls.length);
        resolveAllTriggers();
        return findPermanent(player1, "Clown Car");
    }

    private static final class FixedDiceRollService extends DiceRollService {

        private final int[] results;
        private int index;

        private FixedDiceRollService(int... results) {
            this.results = results;
        }

        @Override
        public int roll(int sides) {
            assertThat(sides).isEqualTo(6);
            return results[index++];
        }
    }
}
