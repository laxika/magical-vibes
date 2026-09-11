package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed(LightfootRogue.class)
class LightfootRogueTest extends BaseCardTest {

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
    @DisplayName("A result from 1 through 9 grants deathtouch")
    void lowRollGrantsDeathtouch() {
        Permanent rogue = attackWithRoll(9);

        assertThat(rogue.getPowerModifier()).isZero();
        assertThat(rogue.getGrantedKeywords()).containsExactly(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("A result from 10 through 19 grants +1/+0 and deathtouch")
    void middleRollBoostsAndGrantsDeathtouch() {
        Permanent rogue = attackWithRoll(10);

        assertThat(rogue.getPowerModifier()).isEqualTo(1);
        assertThat(rogue.getToughnessModifier()).isZero();
        assertThat(rogue.getGrantedKeywords()).containsExactly(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("A result of 20 grants +3/+0, first strike, and deathtouch")
    void criticalRollBoostsAndGrantsKeywords() {
        Permanent rogue = attackWithRoll(20);

        assertThat(rogue.getPowerModifier()).isEqualTo(3);
        assertThat(rogue.getToughnessModifier()).isZero();
        assertThat(rogue.getGrantedKeywords())
                .containsExactlyInAnyOrder(Keyword.FIRST_STRIKE, Keyword.DEATHTOUCH);
    }

    private Permanent attackWithRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
        Permanent rogue = addCreatureReady(player1, new LightfootRogue());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        return rogue;
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
