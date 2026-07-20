package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PathmakerInitiateTest extends BaseCardTest {

    private Permanent addInitiate() {
        Permanent initiate = harness.addToBattlefieldAndReturn(player1, new PathmakerInitiate());
        initiate.setSummoningSick(false);
        return initiate;
    }

    @Test
    @DisplayName("Makes a power-2-or-less creature unblockable this turn, then it wears off")
    void makesCreatureUnblockable() {
        addInitiate();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Tap cost taps Pathmaker Initiate")
    void tapCostTapsInitiate() {
        Permanent initiate = addInitiate();
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, targetId);

        assertThat(initiate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetHighPowerCreature() {
        addInitiate();
        harness.addToBattlefield(player1, new HillGiant()); // 3/3

        UUID targetId = harness.getPermanentId(player1, "Hill Giant");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
