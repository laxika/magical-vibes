package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HoardingOgre.class)
class HoardingOgreTest extends BaseCardTest {

    private RollD20EffectHandler rollD20EffectHandler;
    private D20RollService originalD20RollService;

    @BeforeEach
    void captureD20RollService() {
        rollD20EffectHandler = GameTestEngineContext.get().getBean(RollD20EffectHandler.class);
        originalD20RollService = (D20RollService) ReflectionTestUtils.getField(
                rollD20EffectHandler, "d20RollService");
    }

    @AfterEach
    void restoreD20RollService() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", originalD20RollService);
    }

    @Test
    @DisplayName("A result from 1 through 9 creates one Treasure")
    void lowRollCreatesOneTreasure() {
        attackWithRoll(9);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("A result from 10 through 19 creates two Treasures")
    void middleRollCreatesTwoTreasures() {
        attackWithRoll(10);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    @DisplayName("A result of 20 creates three Treasures")
    void maximumRollCreatesThreeTreasures() {
        attackWithRoll(20);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
    }

    @Test
    void minimumRollCreatesOneTreasure() {
        attackWithRoll(1);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isZero();
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
    }

    @Test
    void highestMiddleRollCreatesTwoTreasures() {
        attackWithRoll(19);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    void treasuresAreCreatedOnlyWhenAttackTriggerResolves() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(20));
        addCreatureReady(player1, new HoardingOgre());
        declareAttackers(List.of(0));

        assertThat(countPermanents(player1, "Treasure")).isZero();

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
    }

    @Test
    void nonattackingOgreDoesNotCreateAdditionalTreasures() {
        addCreatureReady(player1, new HoardingOgre());

        attackWithRoll(20);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
    }

    @Test
    void eachAttackingOgreCreatesItsOwnTreasures() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(20));
        addCreatureReady(player1, new HoardingOgre());
        addCreatureReady(player1, new HoardingOgre());
        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(6);
    }

    @Test
    void opponentControlledOgreCreatesTreasuresForItsController() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(20));
        addCreatureReady(player2, new HoardingOgre());
        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Treasure")).isEqualTo(3);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    private void attackWithRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
        addCreatureReady(player1, new HoardingOgre());
        declareAttackers(List.of(0));
        resolveAllTriggers();
    }

    private static final class FixedD20RollService extends D20RollService {

        private final int result;

        private FixedD20RollService(int result) {
            this.result = result;
        }

        @Override
        public int roll() {
            return result;
        }
    }
}
