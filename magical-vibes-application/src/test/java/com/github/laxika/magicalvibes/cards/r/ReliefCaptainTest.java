package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MakindiAeronaut;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReliefCaptain.class, MakindiAeronaut.class})
class ReliefCaptainTest extends BaseCardTest {

    @Test
    void supportsUpToThreeOtherCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        harness.setHand(player1, List.of(new ReliefCaptain()));
        addMana();

        harness.castCreature(player1, 0, List.of(first.getId(), second.getId(), third.getId()));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void maySupportFewerThanThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        harness.setHand(player1, List.of(new ReliefCaptain()));
        addMana();

        harness.castCreature(player1, 0, List.of(first.getId()));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(third.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTargetItself() {
        harness.setHand(player1, List.of(new ReliefCaptain()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        UUID captainId = harness.getPermanentId(player1, "Relief Captain");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, captainId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayChooseNoTargetsEvenWhenOtherCreaturesExist() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        harness.setHand(player1, List.of(new ReliefCaptain()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Relief Captain")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void supportsCreaturesControlledByEitherPlayer() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new MakindiAeronaut());
        harness.setHand(player1, List.of(new ReliefCaptain()));
        addMana();

        harness.castCreature(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        resolveAllTriggers();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Relief Captain")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canSupportAnotherReliefCaptain() {
        Permanent otherCaptain = harness.addToBattlefieldAndReturn(player1, new ReliefCaptain());
        harness.setHand(player1, List.of(new ReliefCaptain()));
        addMana();

        harness.castCreature(player1, 0, List.of(otherCaptain.getId()));
        resolveAllTriggers();

        assertThat(otherCaptain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Relief Captain")).hasSize(2);
        assertThat(findPermanents(player1, "Relief Captain").get(1)
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void stillSupportsRemainingTargetWhenOneTargetLeavesBattlefield() {
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        harness.setHand(player1, List.of(new ReliefCaptain()));
        addMana();

        harness.castCreature(player1, 0, List.of(removed.getId(), remaining.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(removed);
        gd.playerGraveyards.get(player1.getId()).add(removed.getCard());
        resolveAllTriggers();

        assertThat(remaining.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(removed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSupportTheSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent unselected = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        harness.setHand(player1, List.of(new ReliefCaptain()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(unselected.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

}
