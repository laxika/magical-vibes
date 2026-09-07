package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({FaridehsFireball.class, DarksteelColossus.class, ChandraNalaar.class})
class FaridehsFireballTest extends BaseCardTest {

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
    @DisplayName("Deals 5 damage to a creature, then 2 damage to each player on a low roll")
    void lowRollDealsDamageToEachPlayer() {
        setRoll(9);
        Permanent target = addCreatureReady(player2, new DarksteelColossus());
        castAt(target);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 5 damage to a planeswalker, then 2 damage to each opponent on a high roll")
    void highRollDealsDamageToEachOpponent() {
        setRoll(10);
        Permanent target = new Permanent(new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 10);
        gd.playerBattlefields.get(player2.getId()).add(target);
        castAt(target);

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new FaridehsFireball()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
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
