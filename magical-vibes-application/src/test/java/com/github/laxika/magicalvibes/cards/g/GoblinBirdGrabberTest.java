package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinBirdGrabberTest extends BaseCardTest {

    @Test
    @DisplayName("Gains flying when its controller controls a creature with flying")
    void gainsFlyingWithAnotherFlyingCreature() {
        Permanent grabber = harness.addToBattlefieldAndReturn(player1, new GoblinBirdGrabber());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, grabber, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without controlling a creature with flying")
    void cannotActivateWithoutFlyingCreature() {
        harness.addToBattlefield(player1, new GoblinBirdGrabber());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if you control a creature with flying");
    }

    @Test
    @DisplayName("Flying wears off at the end of the turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent grabber = harness.addToBattlefieldAndReturn(player1, new GoblinBirdGrabber());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, grabber, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, grabber, Keyword.FLYING)).isFalse();
    }
}
