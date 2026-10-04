package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcyBlast.class, AlpineGrizzly.class, WetlandSambar.class, Forest.class})
class IcyBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Taps X target creatures")
    void tapsXTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstantForX(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(first.getSkipUntapCount()).isZero();
        assertThat(second.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Ferocious keeps the targeted creatures tapped through their next untap step")
    void ferociousLocksTargetedCreatures() {
        harness.addToBattlefield(player1, new AlpineGrizzly());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantForX(player1, 0, 1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not lock targets without ferocious")
    void doesNotLockWithoutFerocious() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantForX(player1, 0, 1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void cannotChooseFewerTargetsThanX() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseNoTargetsForPositiveX() {
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void zeroXResolvesWithoutTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getSkipUntapCount()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof IcyBlast);
    }

    @Test
    void ferociousIsCheckedWhenSpellResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstantForX(player1, 0, 1, List.of(target.getId()));

        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.passBothPriorities();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void losingFerociousBeforeResolutionDoesNotLockTargets() {
        Permanent grizzly = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstantForX(player1, 0, 1, List.of(target.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(grizzly);
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void locksAlreadyTappedTargetsForEachControllersNextUntapOnly() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        opposing.tap();
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstantForX(player1, 0, 2, List.of(own.getId(), opposing.getId()));
        harness.passBothPriorities();
        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isTrue();
        assertThat(own.getSkipUntapCount()).isEqualTo(1);
        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isFalse();
        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isFalse();
    }

    @Test
    void canChooseMoreThanOneHundredTargets() {
        List<Permanent> targets = IntStream.range(0, 101)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new WetlandSambar()))
                .toList();
        List<UUID> targetIds = targets.stream().map(Permanent::getId).toList();
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 102);

        harness.castInstantForX(player1, 0, 101, targetIds);
        harness.passBothPriorities();

        assertThat(targets).allMatch(Permanent::isTapped);
    }

    @Test
    void opposingFerociousCreatureDoesNotEnableLock() {
        harness.addToBattlefield(player1, new WetlandSambar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstantForX(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void remainingLegalTargetStillGetsTappedAndLocked() {
        harness.addToBattlefield(player1, new AlpineGrizzly());
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstantForX(player1, 0, 2, List.of(removed.getId(), remaining.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(removed.isTapped()).isFalse();
        assertThat(removed.getSkipUntapCount()).isZero();
        assertThat(remaining.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(remaining.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(remaining.isTapped()).isFalse();
    }

    @Test
    void cannotChooseMoreTargetsThanX() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WetlandSambar());
        harness.setHand(player1, List.of(new IcyBlast()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
