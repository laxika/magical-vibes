package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DonatelloRadScientist.class, GrizzlyBears.class})
class DonatelloRadScientistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps up to three opposing creatures and puts a stun counter on each")
    void etbTapsAndStunsUpToThreeOpposingCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castDonatello(List.of(first.getId(), second.getId(), third.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(first.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can enter with no targets")
    void canEnterWithNoTargets() {
        castDonatello(List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DonatelloRadScientist()));
        addManaForDonatello();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Already tapped creatures receive stun counters and skip their next untap")
    void alreadyTappedCreatureReceivesStunCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DonatelloRadScientist());
        target.tap();

        castDonatello(List.of(target.getId()));

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Choosing only one target leaves other opposing creatures unaffected")
    void canChooseOnlyOneTarget() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castDonatello(List.of(chosen.getId()));

        assertThat(chosen.isTapped()).isTrue();
        assertThat(chosen.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(unchosen.isTapped()).isFalse();
        assertThat(unchosen.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("A target leaving before resolution does not prevent tapping and stunning remaining targets")
    void resolvesForRemainingTargets() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DonatelloRadScientist()));
        addManaForDonatello();
        harness.castCreature(player1, 0, List.of(removed.getId(), remaining.getId()));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.setGraveyard(player2, List.of(removed.getCard()));
        harness.passBothPriorities();

        assertThat(remaining.isTapped()).isTrue();
        assertThat(remaining.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(removed.getCounterCount(CounterType.STUN)).isZero();
    }
    private void castDonatello(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new DonatelloRadScientist()));
        addManaForDonatello();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addManaForDonatello() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
