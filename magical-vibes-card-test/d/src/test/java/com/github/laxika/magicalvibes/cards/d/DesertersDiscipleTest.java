package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesertersDisciple.class, GrizzlyBears.class, HillGiant.class})
class DesertersDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability makes another small creature you control unblockable")
    void resolvingMakesAnotherSmallCreatureUnblockable() {
        Permanent disciple = addDisciple(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isCantBeBlocked()).isTrue();
        assertThat(disciple.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Activating the ability taps Deserter's Disciple")
    void activatingTapsSource() {
        Permanent disciple = addDisciple(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(disciple.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The unblockable effect wears off during cleanup")
    void unblockableWearsOffAtCleanup() {
        addDisciple(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addDisciple(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target Deserter's Disciple itself")
    void cannotTargetSelf() {
        Permanent disciple = addDisciple(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, disciple.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a creature with power greater than 2")
    void cannotTargetHighPowerCreature() {
        addDisciple(player1);
        Permanent target = addCreatureReady(player1, new HillGiant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetAnotherDisciple() {
        addDisciple(player1);
        Permanent target = addDisciple(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void summoningSickDiscipleCannotActivate() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DesertersDisciple());
        Permanent target = addDisciple(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(disciple.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetWithIncreasedPowerIsIllegalOnResolution() {
        addDisciple(player1);
        Permanent target = addDisciple(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void targetControlledByOpponentIsIllegalOnResolution() {
        addDisciple(player1);
        Permanent target = addDisciple(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void increasedPowerAfterResolutionDoesNotEndUnblockability() {
        addDisciple(player1);
        Permanent target = addDisciple(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        target.setPowerModifier(1);

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent disciple = addDisciple(player1);
        Permanent target = addDisciple(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(disciple);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    private Permanent addDisciple(Player player) {
        return addCreatureReady(player, new DesertersDisciple());
    }
}
