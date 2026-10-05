package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LittleBear.class, GrizzlyBears.class, HillGiant.class})
class LittleBearTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps another Bear and puts a +1/+1 counter on it")
    void untapsBearAndPutsCounterOnIt() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.tap();

        castLittleBear(bear);

        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Untaps another non-Bear creature without putting a counter on it")
    void untapsNonBearWithoutCounter() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        giant.tap();

        castLittleBear(giant);

        assertThat(giant.isTapped()).isFalse();
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LittleBear()));
        addLittleBearMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    private void castLittleBear(Permanent target) {
        harness.setHand(player1, List.of(new LittleBear()));
        addLittleBearMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An already untapped Bear still gets a counter")
    void untappedBearStillGetsCounter() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new LittleBear());

        castLittleBear(bear);

        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The enter ability resolves even after Little Bear leaves")
    void abilityResolvesAfterSourceLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new LittleBear());
        bear.tap();
        LittleBear source = new LittleBear();
        harness.setHand(player1, List.of(source));
        addLittleBearMana();
        harness.castCreature(player1, 0, 0, bear.getId());
        harness.passBothPriorities();
        Permanent sourcePermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(source.getId()))
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, sourcePermanent));

        harness.passBothPriorities();

        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A target that leaves before resolution gets no counter")
    void targetLeavesBeforeResolution() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new LittleBear());
        bear.tap();
        harness.setHand(player1, List.of(new LittleBear()));
        addLittleBearMana();
        harness.castCreature(player1, 0, 0, bear.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, bear));

        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Little Bear can enter with no other creature to target")
    void entersWithoutAnotherCreature() {
        harness.castFromHand(player1, new LittleBear(), "{2}{G}");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addLittleBearMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's end step")
    void canCastDuringOpponentsEndStep() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new LittleBear());
        bear.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        castLittleBear(bear);

        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
