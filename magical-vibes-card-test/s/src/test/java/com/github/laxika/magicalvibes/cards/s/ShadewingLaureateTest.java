package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadewingLaureate.class, DoomBlade.class, GrizzlyBears.class, WindDrake.class})
class ShadewingLaureateTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a creature you control when another flying creature you control dies")
    void putsCounterWhenFlyingAllyDies() {
        harness.addToBattlefield(player1, new ShadewingLaureate());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player1, new WindDrake());

        destroyCreature(flyingCreature);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when a nonflying creature you control dies")
    void doesNotTriggerForNonflyingAlly() {
        harness.addToBattlefield(player1, new ShadewingLaureate());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroyCreature(dyingCreature);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canPutCounterOnItself() {
        Permanent laureate = harness.addToBattlefieldAndReturn(player1, new ShadewingLaureate());
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player1, new WindDrake());

        destroyCreature(flyingCreature);
        harness.handlePermanentChosen(player1, laureate.getId());
        harness.passBothPriorities();

        assertThat(laureate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForOpponentsFlyingCreature() {
        Permanent laureate = harness.addToBattlefieldAndReturn(player1, new ShadewingLaureate());
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new WindDrake());

        destroyCreature(flyingCreature);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(laureate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForItsOwnDeath() {
        Permanent laureate = harness.addToBattlefieldAndReturn(player1, new ShadewingLaureate());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        laureate.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Shadewing Laureate");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotPutCounterOnTargetThatDiesBeforeResolution() {
        harness.addToBattlefield(player1, new ShadewingLaureate());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player1, new WindDrake());

        destroyCreature(flyingCreature);
        harness.handlePermanentChosen(player1, target.getId());
        destroyCreature(target);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void destroyCreature(Permanent creature) {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
    }
}
