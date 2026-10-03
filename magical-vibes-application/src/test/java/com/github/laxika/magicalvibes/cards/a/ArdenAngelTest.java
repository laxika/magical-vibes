package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.service.effect.normalfx.D4RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD4EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
    void doesNotRollAfterLeavingGraveyardBeforeResolution() {
        FixedD4RollService roller = setRoll(1);
        ArdenAngel angel = new ArdenAngel();
        harness.setGraveyard(player1, List.of(angel));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(angel));
        harness.passBothPriorities();

        assertThat(roller.rollCount).isZero();
        harness.assertNotOnBattlefield(player1, "Arden Angel");
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        FixedD4RollService roller = setRoll(1);
        harness.setGraveyard(player1, List.of(new ArdenAngel()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(roller.rollCount).isZero();
        harness.assertInGraveyard(player1, "Arden Angel");
        harness.assertNotOnBattlefield(player1, "Arden Angel");
    }

    @Test
    void doesNotTriggerFromBattlefield() {
        FixedD4RollService roller = setRoll(1);
        harness.addToBattlefield(player1, new ArdenAngel());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(roller.rollCount).isZero();
    }

    @Test
    void eachGraveyardCopyReturnsIndependently() {
        FixedD4RollService roller = setRoll(1);
        ArdenAngel first = new ArdenAngel();
        ArdenAngel second = new ArdenAngel();
        harness.setGraveyard(player1, List.of(first, second));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(roller.rollCount).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(first.getId(), second.getId());
        harness.assertNotInGraveyard(player1, "Arden Angel");
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

    private FixedD4RollService setRoll(int result) {
        FixedD4RollService roller = new FixedD4RollService(result);
        ReflectionTestUtils.setField(rollD4EffectHandler, "d4RollService", roller);
        return roller;
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 4})
    void higherDieResultsDoNotReturnAngel(int result) {
        FixedD4RollService roller = setRoll(result);
        harness.setGraveyard(player1, List.of(new ArdenAngel()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(roller.rollCount).isEqualTo(1);
        harness.assertInGraveyard(player1, "Arden Angel");
        harness.assertNotOnBattlefield(player1, "Arden Angel");
    }

    private static final class FixedD4RollService extends D4RollService {

        private final int result;
        private int rollCount;

        private FixedD4RollService(int result) {
            this.result = result;
        }

        @Override
        public int roll() {
            rollCount++;
            return result;
        }
    }
}
