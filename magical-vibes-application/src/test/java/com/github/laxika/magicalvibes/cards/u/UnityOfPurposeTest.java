package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.m.MakindiAeronaut;
import com.github.laxika.magicalvibes.cards.w.Wastes;
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

@CardUsed({UnityOfPurpose.class, MakindiAeronaut.class, Wastes.class})
class UnityOfPurposeTest extends BaseCardTest {

    @Test
    @DisplayName("Supports up to two creatures and untaps creatures with +1/+1 counters")
    void supportsTwoCreaturesAndUntapsCounteredCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent alreadyCountered = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new MakindiAeronaut());
        first.tap();
        second.tap();
        alreadyCountered.tap();
        opponent.tap();
        alreadyCountered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castUnityOfPurpose(List.of(first.getId(), opponent.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.isTapped()).isFalse();
        assertThat(alreadyCountered.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        assertThat(opponent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May choose no creatures")
    void mayChooseNoCreatures() {
        castUnityOfPurpose(List.of());

        harness.assertInGraveyard(player1, "Unity of Purpose");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Wastes());
        harness.setHand(player1, List.of(new UnityOfPurpose()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castUnityOfPurpose(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new UnityOfPurpose()));
        addMana();
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    @Test
    void noTargetsStillUntapsOnlyControlledCreaturesWithPlusOneCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new MakindiAeronaut());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Wastes());
        Permanent otherCounter = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        for (Permanent permanent : List.of(creature, opponent, land, otherCounter)) {
            permanent.tap();
        }
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        otherCounter.setCounterCount(CounterType.CHARGE, 1);

        castUnityOfPurpose(List.of());

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.isTapped()).isTrue();
        assertThat(land.isTapped()).isTrue();
        assertThat(otherCounter.isTapped()).isTrue();
    }

    @Test
    void oneRemainingLegalTargetReceivesCounterAndUntaps() {
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        remaining.tap();
        harness.setHand(player1, List.of(new UnityOfPurpose()));
        addMana();
        harness.castInstant(player1, 0, List.of(removed.getId(), remaining.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(removed);

        harness.passBothPriorities();

        assertThat(remaining.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(remaining.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Unity of Purpose");
    }

    @Test
    void allTargetsIllegalPreventsUntappingUntargetedCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent countered = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        countered.tap();
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new UnityOfPurpose()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(countered.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Unity of Purpose");
    }

    @Test
    void cannotChooseSameCreatureTwiceOrMoreThanTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        harness.setHand(player1, List.of(new UnityOfPurpose()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
