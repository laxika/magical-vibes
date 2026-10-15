package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Greenbelt Guardian")
@CardUsed({GreenbeltGuardian.class, Forest.class})
class GreenbeltGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability grants trample to a target creature")
    void grantsTrampleToTargetCreature() {
        harness.addToBattlefieldAndReturn(player1, new GreenbeltGuardian());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreenbeltGuardian());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Granted trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        harness.addToBattlefieldAndReturn(player1, new GreenbeltGuardian());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GreenbeltGuardian());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The exhaust ability puts three +1/+1 counters on Greenbelt Guardian")
    void exhaustPutsThreeCountersOnIt() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GreenbeltGuardian());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The exhaust ability can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        harness.addToBattlefieldAndReturn(player1, new GreenbeltGuardian());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("The trample ability cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefieldAndReturn(player1, new GreenbeltGuardian());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The trample ability can target Greenbelt Guardian itself")
    void canGrantTrampleToItself() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GreenbeltGuardian());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, guardian.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The trample ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefieldAndReturn(player1, new GreenbeltGuardian());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exhaust cannot be activated again while its first activation is on the stack")
    void exhaustIsConsumedAtActivation() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new GreenbeltGuardian());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");

        harness.passBothPriorities();
        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each Greenbelt Guardian has its own exhaust activation")
    void exhaustIsIndependentForEachPermanent() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GreenbeltGuardian());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GreenbeltGuardian());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
