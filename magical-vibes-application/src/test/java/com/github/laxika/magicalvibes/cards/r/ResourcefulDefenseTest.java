package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResourcefulDefense.class, GrizzlyBears.class})
class ResourcefulDefenseTest extends BaseCardTest {

    @Test
    void transfersAllCounterKindsFromAControlledPermanentThatLeaves() {
        harness.addToBattlefieldAndReturn(player1, new ResourcefulDefense());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        leaving.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        leaving.setCounterCount(CounterType.CHARGE, 3);

        removePermanent(leaving);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(recipient.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void doesNotTriggerForAControlledPermanentWithoutCounters() {
        harness.addToBattlefieldAndReturn(player1, new ResourcefulDefense());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        removePermanent(leaving);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void ignoresAnOpponentsPermanentLeaving() {
        harness.addToBattlefieldAndReturn(player1, new ResourcefulDefense());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent leaving = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        leaving.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        removePermanent(leaving);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void transfersCountersWhenResourcefulDefenseItselfLeaves() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent defense = harness.addToBattlefieldAndReturn(player1, new ResourcefulDefense());
        defense.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        defense.setCounterCount(CounterType.CHARGE, 1);

        removePermanent(defense);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(recipient.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void movesAnyChosenNumberOfEachCounterKind() {
        harness.addToBattlefieldAndReturn(player1, new ResourcefulDefense());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        source.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId(), destination.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1");
        harness.handleListChoice(player1, "1");

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void activatedAbilityRequiresControlledPermanents() {
        harness.addToBattlefieldAndReturn(player1, new ResourcefulDefense());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId(), opponentPermanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(HardenedScales.class)
    void counterReplacementDoesNotIncreaseTheNumberRemovedFromTheSource() {
        harness.addToBattlefieldAndReturn(player1, new ResourcefulDefense());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HardenedScales());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId(), destination.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1");

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void mayChooseToMoveZeroCounters() {
        harness.addToBattlefieldAndReturn(player1, new ResourcefulDefense());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId(), destination.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "0");

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canActivateWithACounterlessSource() {
        harness.addToBattlefieldAndReturn(player1, new ResourcefulDefense());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId(), destination.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void activatedAbilityRequiresTwoDifferentPermanents() {
        harness.addToBattlefieldAndReturn(player1, new ResourcefulDefense());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotRemoveCountersWhenTheDestinationLeavesBeforeResolution() {
        harness.addToBattlefieldAndReturn(player1, new ResourcefulDefense());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(source.getId(), destination.getId()));
        removePermanent(destination);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void transfersCountersToAnEnchantmentWhenAPermanentIsExiled() {
        Permanent defense = harness.addToBattlefieldAndReturn(player1, new ResourcefulDefense());
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        leaving.setCounterCount(CounterType.CHARGE, 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, leaving));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, defense.getId());
        harness.passBothPriorities();

        assertThat(defense.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    private void removePermanent(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
