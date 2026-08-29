package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GirdForBattleTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each of two target creatures")
    void putsCountersOnTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGirdForBattle(List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("May target only one creature")
    void putsCounterOnOneTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGirdForBattle(List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("May choose no targets")
    void mayChooseNoTargets() {
        harness.setHand(player1, List.of(new GirdForBattle()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Gird for Battle");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GirdForBattle()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castGirdForBattle(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new GirdForBattle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }
}
