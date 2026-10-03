package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.model.Keyword;
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
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({ArborArmament.class, BalothGorger.class, ShortSword.class})
class ArborArmamentTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Arbor Armament puts a +1/+1 counter and grants reach to target creature")
    void putsCounterAndGrantsReach() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalothGorger());
        harness.setHand(player1, List.of(new ArborArmament()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("+1/+1 counter persists but reach expires at end of turn")
    void counterPersistsReachExpires() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalothGorger());
        harness.setHand(player1, List.of(new ArborArmament()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        harness.setHand(player1, List.of(new ArborArmament()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.hasKeyword(Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new BalothGorger());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ShortSword());
        harness.setHand(player1, List.of(new ArborArmament()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Arbor Armament fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BalothGorger());
        harness.setHand(player1, List.of(new ArborArmament()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Repeated casts accumulate counters only on the chosen creature")
    void repeatedCastsAccumulateCountersOnSameTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BalothGorger());
        harness.setHand(player1, List.of(new ArborArmament(), new ArborArmament()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.REACH)).isTrue();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.hasKeyword(Keyword.REACH)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.REACH)).isFalse();
    }
}
