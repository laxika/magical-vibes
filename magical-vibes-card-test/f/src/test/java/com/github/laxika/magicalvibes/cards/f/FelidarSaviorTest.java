package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BearCub;
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

@CardUsed({FelidarSavior.class, BearCub.class})
class FelidarSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on up to two other creatures you control")
    void putsCountersOnTwoOtherCreaturesYouControl() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BearCub());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BearCub());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new BearCub());

        harness.setHand(player1, List.of(new FelidarSavior()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB may target only one other creature")
    void mayTargetOneOtherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearCub());

        harness.setHand(player1, List.of(new FelidarSavior()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new BearCub());

        harness.setHand(player1, List.of(new FelidarSavior()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canEnterWithNoOtherCreatures() {
        harness.castFromHand(player1, new FelidarSavior(), "{3}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Felidar Savior");
        assertThat(findPermanent(player1, "Felidar Savior").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDeclineAllTargetsWhenAnotherCreatureIsAvailable() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearCub());
        harness.enterBattlefieldAndReturn(player1, new FelidarSavior());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Felidar Savior").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotTargetItselfWhenEnteringWithoutBeingCast() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearCub());
        Permanent savior = harness.enterBattlefieldAndReturn(player1, new FelidarSavior());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, savior.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(savior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotChooseThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BearCub());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BearCub());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new BearCub());
        harness.setHand(player1, List.of(new FelidarSavior()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseTheSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearCub());
        harness.setHand(player1, List.of(new FelidarSavior()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyStillControlledTargetReceivesCounterAtResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BearCub());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BearCub());
        harness.setHand(player1, List.of(new FelidarSavior()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerBattlefields.get(player2.getId()).add(first);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void abilityStillResolvesAfterSaviorLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BearCub());
        harness.setHand(player1, List.of(new FelidarSavior()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        Permanent savior = findPermanent(player1, "Felidar Savior");
        gd.playerBattlefields.get(player1.getId()).remove(savior);
        gd.playerGraveyards.get(player1.getId()).add(savior.getCard());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Felidar Savior");
    }

    @Test
    void combatDamageGainsLifeThroughLifelink() {
        addCreatureReady(player1, new FelidarSavior());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }
}
