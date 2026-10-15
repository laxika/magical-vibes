package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mintstrosity;
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

@CardUsed({SuccumbToTheCold.class, Forest.class, Mintstrosity.class})
class SuccumbToTheColdTest extends BaseCardTest {

    @Test
    @DisplayName("Taps one target opponent creature and puts a stun counter on it")
    void tapsAndStunsOneCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());

        cast(List.of(target));

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps and stuns two target opponent creatures")
    void tapsAndStunsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());

        cast(List.of(first, second));

        assertThat(first.isTapped()).isTrue();
        assertThat(first.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(second.isTapped()).isTrue();
        assertThat(second.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires at least one target")
    void requiresAtLeastOneTarget() {
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the spell's controller")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Mintstrosity());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Adds a stun counter even if the target is already tapped")
    void stunsAlreadyTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        target.tap();

        cast(List.of(target));

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A stun counter replaces the next untap and permits the following untap")
    void stunReplacesNextUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        cast(List.of(target));

        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Repeated casts stack stun counters and consume one per untap")
    void repeatedCastsStackStunCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        cast(List.of(target));
        cast(List.of(target));

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot select more than two creatures")
    void cannotTargetThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot select the same creature twice")
    void cannotRepeatTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only the remaining legal target is tapped and stunned after a control change")
    void skipsTargetNowControlledByCaster() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        prepareCast();
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerBattlefields.get(player1.getId()).add(first);

        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(first.getCounterCount(CounterType.STUN)).isZero();
        assertThat(second.isTapped()).isTrue();
        assertThat(second.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not resolve when its only target becomes controlled by the caster")
    void doesNotResolveWithNoLegalTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        prepareCast();
        harness.castInstant(player1, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.assertInGraveyard(player1, "Succumb to the Cold");
    }
    @Test
    @DisplayName("Resolves on the remaining creature when the first target leaves the battlefield")
    void resolvesWhenFirstTargetLeavesBattlefield() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Mintstrosity());
        prepareCast();
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        harness.setGraveyard(player2, List.of(first.getCard()));

        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(first.getCounterCount(CounterType.STUN)).isZero();
        assertThat(second.isTapped()).isTrue();
        assertThat(second.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Succumb to the Cold");
    }

    private void cast(List<Permanent> targets) {
        prepareCast();
        harness.castAndResolveInstant(player1, 0, targets.stream().map(Permanent::getId).toList());
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new SuccumbToTheCold()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
