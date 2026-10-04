package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrostBreath.class, RuneclawBear.class, Forest.class, Unsummon.class})
class FrostBreathTest extends BaseCardTest {

    @Test
    @DisplayName("Taps both target creatures and locks their next untap step")
    void tapsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new FrostBreath()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.isTapped()).isTrue();
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("May tap a single creature (up to two)")
    void tapsSingleCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new FrostBreath()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, List.of(bears.getId()));

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new FrostBreath()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canResolveWithoutTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new FrostBreath()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.getSkipUntapCount()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void alreadyTappedCreatureStillSkipsExactlyOneUntap() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.setTapped(true);
        harness.setHand(player1, List.of(new FrostBreath()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, List.of(bear.getId()));

        harness.performUntapStep(player1);
        assertThat(bear.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    void targetsWithDifferentControllersSkipTheirOwnNextUntap() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new FrostBreath()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, List.of(own.getId(), opposing.getId()));

        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isTrue();
        assertThat(opposing.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isFalse();
    }

    @Test
    void remainingTargetIsAffectedWhenOtherTargetLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new FrostBreath(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first);
        assertThat(second.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(second.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    void cannotChooseSameCreatureTwiceOrMoreThanTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new FrostBreath()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
