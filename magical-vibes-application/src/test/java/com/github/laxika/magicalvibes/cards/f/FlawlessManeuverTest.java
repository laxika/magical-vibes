package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlawlessManeuver.class, GrizzlyBears.class})
class FlawlessManeuverTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast without mana while controlling a commander and protects your creatures")
    void freeCastProtectsOwnCreatures() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        commander.setCommander(true);
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlawlessManeuver()));

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mine, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The alternate cost is unavailable without a controlled commander")
    void freeCastRequiresControlledCommander() {
        harness.setHand(player1, List.of(new FlawlessManeuver()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        commander.setCommander(true);
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlawlessManeuver()));

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mine, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
