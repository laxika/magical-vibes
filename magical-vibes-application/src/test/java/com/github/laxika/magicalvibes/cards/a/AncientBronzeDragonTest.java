package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
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

@CardUsed({AncientBronzeDragon.class, GrizzlyBears.class})
class AncientBronzeDragonTest extends BaseCardTest {

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
    void combatDamagePutsTheRollAmountOfCountersOnUpToTwoCreatures() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(7));
        addCreatureReady(player1, new AncientBronzeDragon());
        Permanent firstTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(firstTarget.getId(), secondTarget.getId());
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        resolveAllTriggers();

        assertThat(firstTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(secondTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    void combatDamageMayChooseNoCreatures() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(20));
        addCreatureReady(player1, new AncientBronzeDragon());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
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
