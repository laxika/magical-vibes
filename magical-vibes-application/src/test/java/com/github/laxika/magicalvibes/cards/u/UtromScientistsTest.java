package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UtromScientists.class, Island.class})
class UtromScientistsTest extends BaseCardTest {

    @Test
    void entersAndTapsAndStunsTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UtromScientists());
        cast(List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void mayEnterWithoutTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UtromScientists());
        cast(List.of());

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void putsStunCounterOnAlreadyTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UtromScientists());
        creature.tap();
        cast(List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void canTargetCreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new UtromScientists());
        cast(List.of(creature.getId()));

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void cannotChooseTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new UtromScientists());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new UtromScientists());
        prepareCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stunCounterReplacesNextUntapOnly() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UtromScientists());
        cast(List.of(creature.getId()));

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void mayEnterOnEmptyBattlefieldWithoutTarget() {
        cast(List.of());

        harness.assertOnBattlefield(player1, "Utrom Scientists");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).allSatisfy(permanent -> {
            assertThat(permanent.isTapped()).isFalse();
            assertThat(permanent.getCounterCount(CounterType.STUN)).isZero();
        });
    }

    private void cast(List<java.util.UUID> targetIds) {
        if (targetIds.isEmpty()) {
            harness.castFromHand(player1, new UtromScientists(), "{2}{U}");
        } else {
            prepareCast();
            harness.castCreature(player1, 0, targetIds);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new UtromScientists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
