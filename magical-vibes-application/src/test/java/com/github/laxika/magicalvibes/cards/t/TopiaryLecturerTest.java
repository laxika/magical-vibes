package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TopiaryLecturer.class, GrizzlyBears.class, OneWithTheStars.class})
class TopiaryLecturerTest extends BaseCardTest {

    @BeforeEach
    void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Increment puts a +1/+1 counter on Topiary Lecturer when enough mana is spent")
    void incrementAddsCounter() {
        Permanent lecturer = harness.addToBattlefieldAndReturn(player1, new TopiaryLecturer());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(lecturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping Topiary Lecturer produces green mana equal to its power")
    void tapProducesManaEqualToPower() {
        Permanent lecturer = harness.addToBattlefieldAndReturn(player1, new TopiaryLecturer());
        lecturer.setSummoningSick(false);
        lecturer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(lecturer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equalManaSpentDoesNotIncrement() {
        Permanent lecturer = harness.addToBattlefieldAndReturn(player1, new TopiaryLecturer());
        lecturer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castFromHand(player1, new TopiaryLecturer(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(lecturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void manaSpentGreaterThanOnlyToughnessStillIncrements() {
        Permanent lecturer = harness.addToBattlefieldAndReturn(player1, new TopiaryLecturer());
        lecturer.setPowerModifier(3);

        harness.castFromHand(player1, new TopiaryLecturer(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(lecturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void incrementRechecksStatsAtResolution() {
        Permanent lecturer = harness.addToBattlefieldAndReturn(player1, new TopiaryLecturer());
        harness.castFromHand(player1, new TopiaryLecturer(), "{2}{G}");
        lecturer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.passBothPriorities();

        assertThat(lecturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opponentCastingDoesNotIncrement() {
        Permanent lecturer = harness.addToBattlefieldAndReturn(player1, new TopiaryLecturer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new TopiaryLecturer(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(lecturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void noncreatureDoesNotTriggerIncrement() {
        Permanent lecturer = harness.addToBattlefieldAndReturn(player1, new TopiaryLecturer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new OneWithTheStars());
        aura.setAttachedTo(lecturer.getId());
        assertThat(gqs.isCreature(gd, lecturer)).isFalse();

        harness.castFromHand(player1, new TopiaryLecturer(), "{2}{G}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(lecturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void incrementDoesNothingIfSourceStopsBeingCreatureBeforeResolution() {
        Permanent lecturer = harness.addToBattlefieldAndReturn(player1, new TopiaryLecturer());
        harness.castFromHand(player1, new TopiaryLecturer(), "{2}{G}");
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new OneWithTheStars());
        aura.setAttachedTo(lecturer.getId());
        assertThat(gqs.isCreature(gd, lecturer)).isFalse();

        harness.passBothPriorities();

        assertThat(lecturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void newlyCastLecturerDoesNotIncrementForItsOwnCast() {
        harness.castFromHand(player1, new TopiaryLecturer(), "{2}{G}");
        harness.passBothPriorities();

        Permanent lecturer = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(lecturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void summoningSickLecturerCannotActivateTapAbility() {
        Permanent lecturer = harness.addToBattlefieldAndReturn(player1, new TopiaryLecturer());
        lecturer.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(lecturer.isTapped()).isFalse();
    }
}
