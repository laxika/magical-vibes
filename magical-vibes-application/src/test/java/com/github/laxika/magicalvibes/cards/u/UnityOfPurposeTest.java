package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({UnityOfPurpose.class, GrizzlyBears.class, Island.class})
class UnityOfPurposeTest extends BaseCardTest {

    @Test
    @DisplayName("Supports up to two creatures and untaps creatures with +1/+1 counters")
    void supportsTwoCreaturesAndUntapsCounteredCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent alreadyCountered = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new UnityOfPurpose()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castUnityOfPurpose(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new UnityOfPurpose()));
        addMana();
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
