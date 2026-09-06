package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhaseDolphin.class, GrizzlyBears.class})
class PhaseDolphinTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes another attacking creature unblockable")
    void makesAnotherAttackingCreatureUnblockable() {
        Permanent dolphin = addCreatureReady(player1, new PhaseDolphin());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(bears.isCantBeBlocked()).isTrue();
        assertThat(dolphin.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger cannot target Phase Dolphin itself")
    void cannotTargetItself() {
        Permanent dolphin = addCreatureReady(player1, new PhaseDolphin());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, dolphin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn cleanup")
    void unblockableResetsAtEndOfTurn() {
        addCreatureReady(player1, new PhaseDolphin());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(bears.isCantBeBlocked()).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isFalse();
    }
}
