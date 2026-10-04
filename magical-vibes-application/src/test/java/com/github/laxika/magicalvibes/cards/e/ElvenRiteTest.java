package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Fling;
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

@CardUsed({ElvenRite.class, EndangeredArmodon.class, EnsnaringBridge.class, Fling.class})
class ElvenRiteTest extends BaseCardTest {

    @Test
    @DisplayName("Does not reallocate the counter assigned to a target that leaves the battlefield")
    void survivingTargetReceivesOnlyItsAssignedCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EndangeredArmodon());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EndangeredArmodon());
        harness.setHand(player1, List.of(new ElvenRite(), new Fling()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of(first.getId(), second.getId()));
        harness.castInstantWithSacrifice(player1, 0, player2.getId(), first.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Endangered Armodon");
        harness.assertInGraveyard(player1, "Elven Rite");
    }

    @Test
    @DisplayName("Does not place counters when its only target leaves the battlefield")
    void onlyTargetLeavingPreventsResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EndangeredArmodon());
        harness.setHand(player1, List.of(new ElvenRite(), new Fling()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of(creature.getId()));
        harness.castInstantWithSacrifice(player1, 0, player2.getId(), creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Elven Rite");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot be cast without a target")
    void cannotChooseZeroTargets() {
        harness.setHand(player1, List.of(new ElvenRite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target more than two creatures")
    void cannotChooseThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EndangeredArmodon());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EndangeredArmodon());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new EndangeredArmodon());
        harness.setHand(player1, List.of(new ElvenRite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseDuplicateTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EndangeredArmodon());
        harness.setHand(player1, List.of(new ElvenRite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Puts both +1/+1 counters on one target creature")
    void putsBothCountersOnOneTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EndangeredArmodon());
        harness.setHand(player1, List.of(new ElvenRite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts one +1/+1 counter on each of two target creatures")
    void putsOneCounterOnEachOfTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EndangeredArmodon());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EndangeredArmodon());
        harness.setHand(player1, List.of(new ElvenRite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new EndangeredArmodon());
        harness.setHand(player1, List.of(new ElvenRite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnsnaringBridge());
        harness.setHand(player1, List.of(new ElvenRite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
