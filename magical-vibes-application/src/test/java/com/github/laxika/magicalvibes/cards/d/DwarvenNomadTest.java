package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrinningTotem;
import com.github.laxika.magicalvibes.cards.t.TalruumMinotaur;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenNomad.class, GrinningTotem.class, TalruumMinotaur.class, ZhalfirinKnight.class})
class DwarvenNomadTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability makes a creature with power 2 or less unblockable")
    void makesLowPowerCreatureUnblockable() {
        addCreatureReady(player1, new DwarvenNomad());
        Permanent target = addCreatureReady(player1, new ZhalfirinKnight());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps the Nomad")
    void activatingTapsSelf() {
        Permanent nomad = addCreatureReady(player1, new DwarvenNomad());
        Permanent target = addCreatureReady(player1, new ZhalfirinKnight());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(nomad.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature with power 3 is an illegal target")
    void cannotTargetHighPowerCreature() {
        addCreatureReady(player1, new DwarvenNomad());
        Permanent giant = addCreatureReady(player2, new TalruumMinotaur());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a low-power creature an opponent controls")
    void canTargetOpponentsCreature() {
        addCreatureReady(player1, new DwarvenNomad());
        Permanent target = addCreatureReady(player2, new ZhalfirinKnight());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("A noncreature permanent is an illegal target")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new DwarvenNomad());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GrinningTotem());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability does not resolve if the target's power becomes greater than 2")
    void targetBecomingTooPowerfulFizzlesAbility() {
        addCreatureReady(player1, new DwarvenNomad());
        Permanent target = addCreatureReady(player1, new ZhalfirinKnight());

        harness.activateAbility(player1, 0, null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(target.getEffectivePower()).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new DwarvenNomad());
        Permanent target = addCreatureReady(player1, new ZhalfirinKnight());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }
}
