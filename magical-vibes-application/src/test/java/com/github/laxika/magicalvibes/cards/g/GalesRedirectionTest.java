package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GalesRedirection.class, SuntailHawk.class})
class GalesRedirectionTest extends BaseCardTest {

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
    void lowResultGrantsNormalCastWithAnyMana() {
        setRoll(13);
        SuntailHawk hawk = new SuntailHawk();
        harness.setHand(player1, List.of(hawk));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new GalesRedirection()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hawk.getId());

        assertThat(gd.exilePlayPermissions.get(hawk.getId())).isEqualTo(player2.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(hawk.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(hawk.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFromExile(player2, hawk.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    void highResultGrantsFreeCastWhenManaValueIsAdded() {
        setRoll(14);
        SuntailHawk hawk = new SuntailHawk();
        harness.setHand(player1, List.of(hawk));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new GalesRedirection()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, hawk.getId());

        assertThat(gd.exilePlayPermissions.get(hawk.getId())).isEqualTo(player2.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(hawk.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).doesNotContain(hawk.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, hawk.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    void cannotTargetAPermanent() {
        var permanent = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setHand(player2, List.of(new GalesRedirection()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
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
