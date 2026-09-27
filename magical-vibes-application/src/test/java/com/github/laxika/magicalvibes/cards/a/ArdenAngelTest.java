package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.service.effect.normalfx.D4RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD4EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ArdenAngel.class)
class ArdenAngelTest extends BaseCardTest {

    private RollD4EffectHandler rollD4EffectHandler;
    private D4RollService originalD4RollService;

    @BeforeEach
    void captureD4RollService() {
        rollD4EffectHandler = GameTestEngineContext.get().getBean(RollD4EffectHandler.class);
        originalD4RollService = (D4RollService) ReflectionTestUtils.getField(
                rollD4EffectHandler, "d4RollService");
    }

    @AfterEach
    void restoreD4RollService() {
        ReflectionTestUtils.setField(rollD4EffectHandler, "d4RollService", originalD4RollService);
    }

    @Test
    void returnsFromGraveyardOnOne() {
        setRoll(1);
        ArdenAngel angel = new ArdenAngel();
        harness.setGraveyard(player1, List.of(angel));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(angel.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(angel.getId()));
    }

    @Test
    void staysInGraveyardOnOtherResults() {
        setRoll(2);
        ArdenAngel angel = new ArdenAngel();
        harness.setGraveyard(player1, List.of(angel));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(angel.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(angel.getId()));
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD4EffectHandler, "d4RollService", new FixedD4RollService(result));
    }

    private static final class FixedD4RollService extends D4RollService {

        private final int result;

        private FixedD4RollService(int result) {
            this.result = result;
        }

        @Override
        public int roll() {
            return result;
        }
    }
}
