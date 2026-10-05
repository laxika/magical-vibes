package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    @ParameterizedTest
    @ValueSource(ints = {1, 19})
    @DisplayName("The remaining range boundaries select the correct outcome")
    void remainingRangeBoundaries(int result) {
        Permanent rogue = attackWithRoll(result);

        assertThat(rogue.getPowerModifier()).isEqualTo(result == 1 ? 0 : 1);
        assertThat(rogue.getToughnessModifier()).isZero();
        assertThat(rogue.getGrantedKeywords()).containsExactly(Keyword.DEATHTOUCH);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 20})
    @DisplayName("Every roll outcome expires at the end of the turn")
    void rollOutcomeExpiresAtEndOfTurn(int result) {
        Permanent rogue = attackWithRoll(result);
        assertThat(rogue.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(rogue.getPowerModifier()).isZero();
        assertThat(rogue.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, rogue, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, rogue, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An attacking Rogue does not grant its bonuses to another Rogue")
    void bonusesApplyOnlyToAttackingRogue() {
        Permanent otherRogue = addCreatureReady(player1, new LightfootRogue());
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(20));
        Permanent attacker = addCreatureReady(player1, new LightfootRogue());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(3);
        assertThat(attacker.getGrantedKeywords())
                .containsExactlyInAnyOrder(Keyword.FIRST_STRIKE, Keyword.DEATHTOUCH);
        assertThat(otherRogue.getPowerModifier()).isZero();
        assertThat(otherRogue.getGrantedKeywords()).isEmpty();
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
