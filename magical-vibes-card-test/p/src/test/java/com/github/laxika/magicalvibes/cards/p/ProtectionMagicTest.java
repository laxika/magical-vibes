package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({ProtectionMagic.class, GrizzlyBears.class, Forest.class})
class ProtectionMagicTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a shield counter on each of three target creatures")
    void putsShieldCounterOnEachTargetCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(List.of(first.getId(), second.getId(), third.getId()));

        assertThat(first.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target fewer than three creatures")
    void canTargetFewerThanThreeCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ProtectionMagic()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can resolve without choosing any targets")
    void canResolveWithNoTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(List.of());

        assertThat(creature.getCounterCount(CounterType.SHIELD)).isZero();
        harness.assertInGraveyard(player1, "Protection Magic");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Puts counters on two chosen creatures and leaves others unchanged")
    void canTargetExactlyTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(unchosen.getCounterCount(CounterType.SHIELD)).isZero();
    }

    @Test
    @DisplayName("Cannot choose more than three creatures")
    void cannotTargetFourCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ProtectionMagic()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the same creature more than once")
    void cannotRepeatTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ProtectionMagic()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Still puts a shield counter on remaining targets when one leaves the battlefield")
    void resolvesForRemainingTargets() {
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ProtectionMagic()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, List.of(removed.getId(), remaining.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(remaining.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(removed.getCounterCount(CounterType.SHIELD)).isZero();
        harness.assertInGraveyard(player1, "Protection Magic");
    }

    private void cast(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new ProtectionMagic()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targetIds);
    }
}
