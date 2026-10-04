package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({GavonySilversmith.class, GavonyTrapper.class, Plains.class})
class GavonySilversmithTest extends BaseCardTest {

    @Test
    void putsCounterOnOneTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());
        harness.setHand(player1, List.of(new GavonySilversmith()));
        addMana();

        UUID targetId = target.getId();
        harness.castCreature(player1, 0, List.of(targetId));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void putsCounterOnEachOfTwoTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());
        harness.setHand(player1, List.of(new GavonySilversmith()));
        addMana();

        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void canEnterWithoutTargets() {
        harness.setHand(player1, List.of(new GavonySilversmith()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Gavony Silversmith");
        assertThat(findPermanent(player1, "Gavony Silversmith")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new GavonySilversmith()));
        addMana();

        UUID opponentLandId = harness.getPermanentId(player2, "Plains");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponentLandId)))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    void canTargetItselfAndAnOpponentsCreatureAfterEntering() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GavonyTrapper());
        harness.setHand(player1, List.of(new GavonySilversmith()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent silversmith = findPermanent(player1, "Gavony Silversmith");
        harness.handlePermanentChosen(player1, silversmith.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        resolveAllTriggers();

        assertThat(silversmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void stillPutsCounterOnRemainingTargetWhenOneTargetLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());
        harness.setHand(player1, List.of(new GavonySilversmith()));
        addMana();

        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first));
        resolveAllTriggers();

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSilversmithLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());
        harness.setHand(player1, List.of(new GavonySilversmith()));
        addMana();

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        Permanent silversmith = findPermanent(player1, "Gavony Silversmith");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, silversmith));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Gavony Silversmith");
    }

    @Test
    void cannotChooseSameCreatureTwiceForTheTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GavonyTrapper());
        harness.setHand(player1, List.of(new GavonySilversmith()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

}
