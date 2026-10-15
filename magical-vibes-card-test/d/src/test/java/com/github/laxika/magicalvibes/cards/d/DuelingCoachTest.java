package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.cards.l.LoreholdCampus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuelingCoach.class, EagerFirstYear.class, LoreholdCampus.class})
class DuelingCoachTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature")
    void etbPutsCounterOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.setHand(player1, List.of(new DuelingCoach()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void etbCannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new LoreholdCampus());
        harness.setHand(player1, List.of(new DuelingCoach()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability puts counters only on controlled creatures that already have one")
    void activatedAbilityPutsCountersOnExistingCounterBearers() {
        Permanent coach = addCreatureReady(player1, new DuelingCoach());
        Permanent withCounter = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        Permanent withoutCounter = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear());
        withCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(coach.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(withCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(withoutCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void etbCanTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear());
        harness.setHand(player1, List.of(new DuelingCoach()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void etbCanTargetCoachItself() {
        Permanent coach = harness.enterBattlefieldAndReturn(player1, new DuelingCoach());

        harness.handlePermanentChosen(player1, coach.getId());
        resolveAllTriggers();

        assertThat(coach.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void activatedAbilityIncludesCoachButExcludesOpponentAndNoncreatureCounterBearers() {
        Permanent coach = addCreatureReady(player1, new DuelingCoach());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new LoreholdCampus());
        coach.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        assertThat(coach.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(coach.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void activatedAbilityChecksExistingCountersAtResolution() {
        addCreatureReady(player1, new DuelingCoach());
        Permanent gainingCounter = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        Permanent losingCounter = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        losingCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        gainingCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        losingCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gainingCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(losingCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void activatedAbilityResolvesWithNoEligibleCreatures() {
        Permanent coach = addCreatureReady(player1, new DuelingCoach());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(coach.isTapped()).isTrue();
        assertThat(coach.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activatedAbilityDoesNotAddCountersToCreaturesWithOnlyOtherCounterTypes() {
        addCreatureReady(player1, new DuelingCoach());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        creature.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void activatedAbilityStillResolvesAfterCoachLeavesBattlefield() {
        Permanent coach = addCreatureReady(player1, new DuelingCoach());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(coach);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void activatedAbilityCannotBeUsedWhileTapped() {
        Permanent coach = addCreatureReady(player1, new DuelingCoach());
        coach.tap();
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityCannotBeUsedWhileSummoningSick() {
        harness.addToBattlefield(player1, new DuelingCoach());
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityRequiresFiveManaIncludingWhite() {
        Permanent coach = addCreatureReady(player1, new DuelingCoach());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(coach.isTapped()).isFalse();
    }

    @Test
    void activatedAbilityCannotBePaidWithOnlyGenericMana() {
        Permanent coach = addCreatureReady(player1, new DuelingCoach());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(coach.isTapped()).isFalse();
    }
}
