package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BrazenDwarf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD8EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClayGolem.class, BrazenDwarf.class})
class ClayGolemTest extends BaseCardTest {

    private RollD8EffectHandler effectHandler;
    private DiceRollService originalDiceRollService;

    @BeforeEach
    void captureDiceRollService() {
        effectHandler = GameTestEngineContext.get().getBean(RollD8EffectHandler.class);
        originalDiceRollService = (DiceRollService) ReflectionTestUtils.getField(effectHandler, "diceRollService");
    }

    @AfterEach
    void restoreDiceRollService() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", originalDiceRollService);
    }

    @Test
    void rollsForCountersAndDestroysChosenPermanent() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(5));
        Permanent golem = addCreatureReady(player1, new ClayGolem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClayGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(golem.isMonstrous()).isTrue();
        harness.assertInGraveyard(player2, "Clay Golem");
    }

    @Test
    void canActivateAgainAfterBecomingMonstrousWithoutCountersOrAnotherTrigger() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(1));
        Permanent golem = addCreatureReady(player1, new ClayGolem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClayGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(golem.isMonstrous()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void rollsAsAnActivationCostBeforeEitherPlayerCanRespond() {
        FixedDiceRollService dice = new FixedDiceRollService(8);
        ReflectionTestUtils.setField(effectHandler, "diceRollService", dice);
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new ClayGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);

        assertThat(dice.rollCount).isEqualTo(1);
        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(golem.isMonstrous()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void onlyTheFirstResolvingActivationAddsCountersAndTriggersBerserk() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(8));
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new ClayGolem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ClayGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
        assertThat(golem.isMonstrous()).isTrue();
        harness.assertInGraveyard(player2, "Clay Golem");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void berserkCanTargetTheGolemItself() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(3));
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new ClayGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, golem.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Clay Golem");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void diceRollTriggerResolvesBeforeMonstrosity() {
        ReflectionTestUtils.setField(effectHandler, "diceRollService", new FixedDiceRollService(4));
        Permanent golem = harness.addToBattlefieldAndReturn(player1, new ClayGolem());
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new BrazenDwarf());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(golem.isMonstrous()).isFalse();
        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        assertThat(golem.isMonstrous()).isTrue();
        assertThat(golem.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.handlePermanentChosen(player1, dwarf.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Brazen Dwarf");
    }

    private static final class FixedDiceRollService extends DiceRollService {

        private final int result;
        private int rollCount;

        private FixedDiceRollService(int result) {
            this.result = result;
        }

        @Override
        public int roll(int sides) {
            assertThat(sides).isEqualTo(8);
            rollCount++;
            return result;
        }
    }
}
