package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.service.effect.normalfx.D4RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD4EffectHandler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BucknardsEverfullPurse.class)
class BucknardsEverfullPurseTest extends BaseCardTest {

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
    @DisplayName("Creates Treasures equal to the d4 result, then passes the Purse to the right")
    void createsRolledTreasuresThenPassesToTheRight() {
        setRoll(3);
        Permanent purse = harness.addToBattlefieldAndReturn(player1, new BucknardsEverfullPurse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(purse);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(purse);
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
