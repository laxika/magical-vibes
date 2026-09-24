package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuthlessWaterbender.class, GrizzlyBears.class})
class RuthlessWaterbenderTest extends BaseCardTest {

    @Test
    @DisplayName("Waterbend taps artifacts or creatures and boosts this creature until end of turn")
    void waterbendBoostsThisCreatureUntilEndOfTurn() {
        Permanent waterbender = harness.addToBattlefieldAndReturn(player1, new RuthlessWaterbender());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(waterbender.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, waterbender)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, waterbender)).isEqualTo(3);
    }

    @Test
    @DisplayName("Waterbend can be activated only during your turn")
    void waterbendRequiresYourTurn() {
        harness.addToBattlefieldAndReturn(player1, new RuthlessWaterbender());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }
}
