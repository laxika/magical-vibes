package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD6EffectHandler;
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

@CardUsed({VelukanDragon.class, GrizzlyBears.class})
class VelukanDragonTest extends BaseCardTest {

    private RollD6EffectHandler rollD6EffectHandler;
    private DiceRollService originalDiceRollService;

    @BeforeEach
    void captureDiceRollService() {
        rollD6EffectHandler = GameTestEngineContext.get().getBean(RollD6EffectHandler.class);
        originalDiceRollService = (DiceRollService) ReflectionTestUtils.getField(
                rollD6EffectHandler, "diceRollService");
    }

    @AfterEach
    void restoreDiceRollService() {
        ReflectionTestUtils.setField(rollD6EffectHandler, "diceRollService", originalDiceRollService);
    }

    @Test
    @DisplayName("Attacking rolls a d6 and grants power equal to the result minus one")
    void attackRollBoostsByResultMinusOne() {
        setRoll(6);
        Permanent dragon = addCreatureReady(player1, new VelukanDragon());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(dragon.getEffectivePower()).isEqualTo(10);
    }

    @Test
    @DisplayName("Blocking also rolls a d6 and grants the same temporary boost")
    void blockRollBoostsByResultMinusOne() {
        setRoll(4);
        Permanent dragon = addCreatureReady(player1, new VelukanDragon());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(dragon.getEffectivePower()).isEqualTo(8);
    }

    @Test
    @DisplayName("A roll of one grants no power")
    void minimumRollGrantsNoPower() {
        setRoll(1);
        Permanent dragon = addCreatureReady(player1, new VelukanDragon());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(dragon.getEffectivePower()).isEqualTo(5);
    }

    @Test
    void boostWearsOffAtCleanup() {
        setRoll(6);
        Permanent dragon = addCreatureReady(player1, new VelukanDragon());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(dragon.getEffectivePower()).isEqualTo(10);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getEffectivePower()).isEqualTo(5);
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD6EffectHandler, "diceRollService", new FixedDiceRollService(result));
    }

    private static final class FixedDiceRollService extends DiceRollService {

        private final int result;

        private FixedDiceRollService(int result) {
            this.result = result;
        }

        @Override
        public int roll(int sides) {
            return result;
        }
    }
}
