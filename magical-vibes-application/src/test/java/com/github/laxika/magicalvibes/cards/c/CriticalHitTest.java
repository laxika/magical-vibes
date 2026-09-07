package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({CriticalHit.class, ChaosChanneler.class, GrizzlyBears.class})
class CriticalHitTest extends BaseCardTest {

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
    @DisplayName("Target creature gains double strike until end of turn")
    void grantsDoubleStrikeToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CriticalHit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getGrantedKeywords()).contains(Keyword.DOUBLE_STRIKE);
    }

    @Test
    @DisplayName("Returns itself from the graveyard after a natural 20")
    void returnsFromGraveyardAfterNaturalTwenty() {
        setRoll(20);
        CriticalHit criticalHit = new CriticalHit();
        harness.setGraveyard(player1, List.of(criticalHit));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addCreatureReady(player1, new ChaosChanneler());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(criticalHit);
        assertThat(gd.playerHands.get(player1.getId())).contains(criticalHit);
    }

    @Test
    @DisplayName("Does not return itself after a non-20 roll")
    void staysInGraveyardAfterNonTwenty() {
        setRoll(19);
        CriticalHit criticalHit = new CriticalHit();
        harness.setGraveyard(player1, List.of(criticalHit));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addCreatureReady(player1, new ChaosChanneler());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(criticalHit);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(criticalHit);
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
