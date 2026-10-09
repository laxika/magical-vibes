package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HeartlessAct;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrystallineGiant.class, HeartlessAct.class})
class CrystallineGiantTest extends BaseCardTest {

    private static final List<CounterType> COUNTER_TYPES = List.of(
            CounterType.FLYING,
            CounterType.FIRST_STRIKE,
            CounterType.DEATHTOUCH,
            CounterType.HEXPROOF,
            CounterType.LIFELINK,
            CounterType.MENACE,
            CounterType.REACH,
            CounterType.TRAMPLE,
            CounterType.VIGILANCE,
            CounterType.PLUS_ONE_PLUS_ONE
    );

    @Test
    void putsTheOnlyMissingCounterOnIt() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CrystallineGiant());
        COUNTER_TYPES.stream()
                .filter(counterType -> counterType != CounterType.VIGILANCE)
                .forEach(counterType -> giant.setCounterCount(counterType, 1));

        resolveBeginningOfCombat(player1);

        assertThat(giant.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(COUNTER_TYPES.stream().mapToInt(giant::getCounterCount).sum()).isEqualTo(10);
    }

    @Test
    void doesNotTriggerOnOpponentTurn() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CrystallineGiant());

        resolveBeginningOfCombat(player2);

        assertThat(COUNTER_TYPES.stream().mapToInt(giant::getCounterCount).sum()).isZero();
    }

    @Test
    void doesNothingWhenItHasEveryListedCounter() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CrystallineGiant());
        COUNTER_TYPES.forEach(counterType -> giant.setCounterCount(counterType, 1));

        resolveBeginningOfCombat(player1);

        assertThat(COUNTER_TYPES.stream().mapToInt(giant::getCounterCount).sum()).isEqualTo(10);
    }

    @ParameterizedTest
    @EnumSource(value = CounterType.class, names = {
            "FLYING", "FIRST_STRIKE", "DEATHTOUCH", "HEXPROOF", "LIFELINK",
            "MENACE", "REACH", "TRAMPLE", "VIGILANCE", "PLUS_ONE_PLUS_ONE"
    })
    void canAddEachListedCounterWithoutDuplicatingExistingKinds(CounterType missingType) {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CrystallineGiant());
        COUNTER_TYPES.stream()
                .filter(type -> type != missingType)
                .forEach(type -> giant.setCounterCount(type, 2));
        int powerBefore = gqs.getEffectivePower(gd, giant);
        int toughnessBefore = gqs.getEffectiveToughness(gd, giant);

        resolveBeginningOfCombat(player1);

        assertThat(giant.getCounterCount(missingType)).isEqualTo(1);
        COUNTER_TYPES.stream().filter(type -> type != missingType)
                .forEach(type -> assertThat(giant.getCounterCount(type)).isEqualTo(2));
        if (missingType == CounterType.PLUS_ONE_PLUS_ONE) {
            assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(powerBefore + 1);
            assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(toughnessBefore + 1);
        } else {
            assertThat(gqs.hasKeyword(gd, giant, missingType.grantedKeyword())).isTrue();
        }
    }

    @Test
    void addsExactlyOneListedCounterToAnUnmodifiedGiant() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CrystallineGiant());

        resolveBeginningOfCombat(player1);

        assertThat(COUNTER_TYPES.stream().mapToInt(giant::getCounterCount).sum()).isEqualTo(1);
        COUNTER_TYPES.forEach(type -> assertThat(giant.getCounterCount(type)).isBetween(0, 1));
    }

    @Test
    void choosesFromCountersMissingAtResolutionEvenIfNoneWereMissingWhenItTriggered() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CrystallineGiant());
        COUNTER_TYPES.forEach(type -> giant.setCounterCount(type, 1));

        beginCombat(player1);

        assertThat(gd.stack).hasSize(1);
        giant.setCounterCount(CounterType.VIGILANCE, 0);
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(COUNTER_TYPES.stream().mapToInt(giant::getCounterCount).sum()).isEqualTo(10);
    }

    @Test
    void doesNotAddACounterBeforeTheTriggerResolves() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CrystallineGiant());

        beginCombat(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(COUNTER_TYPES.stream().mapToInt(giant::getCounterCount).sum()).isZero();

        harness.passBothPriorities();

        assertThat(COUNTER_TYPES.stream().mapToInt(giant::getCounterCount).sum()).isEqualTo(1);
    }

    @Test
    void resolvesHarmlesslyIfTheGiantIsDestroyedInResponse() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new CrystallineGiant());
        harness.setHand(player1, List.of(new HeartlessAct()));
        beginCombat(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 0, giant.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Crystalline Giant");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(COUNTER_TYPES.stream().mapToInt(giant::getCounterCount).sum()).isZero();
    }

    private void beginCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
    }

    private void resolveBeginningOfCombat(Player activePlayer) {
        beginCombat(activePlayer);
        harness.passBothPriorities();
    }
}
