package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hubris;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaturesPanoply.class, GrizzlyBears.class, Forest.class, Hubris.class})
class NaturesPanoplyTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each target creature")
    void putsCounterOnEachTargetCreature() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NaturesPanoply()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.of(ownBear.getId(), opposingBear.getId()));

        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can be cast with no targets")
    void castsWithNoTargets() {
        harness.setHand(player1, List.of(new NaturesPanoply()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, List.of());

        harness.assertInGraveyard(player1, "Nature's Panoply");
    }

    @Test
    @DisplayName("Strive requires {2}{G} for each additional target")
    void chargesForEachAdditionalTarget() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NaturesPanoply()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only creature permanents can be targeted")
    void cannotTargetNonCreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new NaturesPanoply()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID forestId = forest.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void oneTargetCostsOnlyOneGreenMana() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NaturesPanoply()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotChooseTheSameCreatureTwice() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NaturesPanoply()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bear.getId(), bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolvesForRemainingTargetWhenAnotherReturnsToHand() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NaturesPanoply()));
        harness.setHand(player2, List.of(new Hubris()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, List.of(firstBear.getId(), secondBear.getId()));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, firstBear.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(firstBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(secondBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Nature's Panoply");
    }
}
