package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BladeInstructor.class, GrizzlyBears.class, HillGiant.class})
class BladeInstructorTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor targets only an attacking creature with lesser power")
    void mentorTargetsAttackingCreatureWithLesserPower() {
        addCreatureReady(player1, new BladeInstructor());
        Permanent attackingBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttackingBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent equalPowerCreature = addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(0, 1, 3));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attackingBears.getId());

        harness.handlePermanentChosen(player1, attackingBears.getId());
        resolveAllTriggers();

        assertThat(attackingBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttackingBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(equalPowerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor uses the source's last known power if it leaves before resolution")
    void mentorUsesSourceLastKnownPower() {
        Permanent instructor = addCreatureReady(player1, new BladeInstructor());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(instructor);
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mentor does not add a counter if the target's power becomes equal to the source's")
    void mentorRechecksTargetPower() {
        addCreatureReady(player1, new BladeInstructor());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mentor compares the source's current power rather than its power when it attacked")
    void mentorRechecksSourcePower() {
        Permanent instructor = addCreatureReady(player1, new BladeInstructor());
        instructor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent giant = addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, giant.getId());
        instructor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        resolveAllTriggers();

        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor does not add a counter to a creature that has left combat")
    void mentorRechecksWhetherTargetIsAttacking() {
        addCreatureReady(player1, new BladeInstructor());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        bears.setAttacking(false);
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mentor has no legal target when Blade Instructor attacks alone")
    void mentorCannotTargetItself() {
        Permanent instructor = addCreatureReady(player1, new BladeInstructor());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(instructor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Two mentor triggers targeting the same creature add only one counter when it reaches equal power")
    void multipleMentorTriggersRecheckPowerIndependently() {
        addCreatureReady(player1, new BladeInstructor());
        addCreatureReady(player1, new BladeInstructor());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
