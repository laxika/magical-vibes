package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ViashinoGrappler.class)
class ViashinoGrapplerTest extends BaseCardTest {

    @Test
    void gainsTrampleUntilEndOfTurn() {
        Permanent grappler = addCreatureReady(player1, new ViashinoGrappler());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, grappler, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, grappler, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void grantsTrampleOnlyToTheSourceCreature() {
        Permanent grappler = addCreatureReady(player1, new ViashinoGrappler());
        Permanent otherGrappler = addCreatureReady(player1, new ViashinoGrappler());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, grappler, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherGrappler, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void requiresGreenMana() {
        addCreatureReady(player1, new ViashinoGrappler());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateTwiceWithoutTapping() {
        Permanent grappler = addCreatureReady(player1, new ViashinoGrappler());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(grappler.isTapped()).isFalse();
    }
}
