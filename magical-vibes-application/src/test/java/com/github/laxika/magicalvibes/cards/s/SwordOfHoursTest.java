package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD12AndResolveIfGreaterThanEventValueEffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfHours.class, GrizzlyBears.class})
class SwordOfHoursTest extends BaseCardTest {

    private RollD12AndResolveIfGreaterThanEventValueEffectHandler rollHandler;
    private DiceRollService originalDiceRollService;

    @BeforeEach
    void captureDiceRollService() {
        rollHandler = GameTestEngineContext.get()
                .getBean(RollD12AndResolveIfGreaterThanEventValueEffectHandler.class);
        originalDiceRollService = (DiceRollService) ReflectionTestUtils.getField(
                rollHandler, "diceRollService");
    }

    @AfterEach
    void restoreDiceRollService() {
        ReflectionTestUtils.setField(rollHandler, "diceRollService", originalDiceRollService);
    }

    @Test
    void equippedCreatureGetsCounterWhenAttacking() {
        Permanent creature = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void rollGreaterThanDamageDoublesCounters() {
        Permanent creature = prepareCombat(5);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void naturalTwelveDoublesCountersEvenWhenNotGreaterThanDamage() {
        Permanent creature = prepareCombat(12);
        creature.setPowerModifier(8);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void rollEqualToDamageDoesNotDoubleCounters() {
        Permanent creature = prepareCombat(4);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent prepareCombat(int roll) {
        ReflectionTestUtils.setField(rollHandler, "diceRollService", new FixedDiceRollService(roll));

        Permanent creature = addCreatureReady(player1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        return creature;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent addSwordReady(Player player) {
        Permanent sword = harness.addToBattlefieldAndReturn(player, new SwordOfHours());
        sword.setSummoningSick(false);
        return sword;
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
