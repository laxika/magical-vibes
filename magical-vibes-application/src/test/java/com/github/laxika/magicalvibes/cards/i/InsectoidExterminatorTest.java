package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InsectoidExterminator.class, Forest.class, GrizzlyBears.class})
class InsectoidExterminatorTest extends BaseCardTest {

    @Test
    @DisplayName("Scries 1 at your end step after a permanent you control leaves the battlefield")
    void scriesAfterYourPermanentLeaves() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new InsectoidExterminator());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Does not scry at your end step when no permanent you control left")
    void doesNotScryWithoutPermanentLeaving() {
        harness.addToBattlefield(player1, new InsectoidExterminator());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's permanent leaving the battlefield does not satisfy the ability")
    void opponentPermanentLeavingDoesNotSatisfyAbility() {
        harness.addToBattlefield(player1, new InsectoidExterminator());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
