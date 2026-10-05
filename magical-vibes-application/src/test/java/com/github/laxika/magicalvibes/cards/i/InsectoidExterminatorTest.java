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

    @Test
    @DisplayName("A land leaving satisfies Disappear")
    void scriesAfterLandLeaves() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new InsectoidExterminator());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, land));
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Multiple departures create only one end-step trigger per Exterminator")
    void multipleDeparturesTriggerOnlyOnce() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new InsectoidExterminator());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, first);
            harness.getPermanentRemovalService().removePermanentToHand(gd, second);
        });
        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger on the opponent's end step even after your permanent leaves")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new InsectoidExterminator());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, land));
        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("A departure before Exterminator enters still satisfies Disappear")
    void scriesForDepartureBeforeEntering() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, land));
        harness.addToBattlefield(player1, new InsectoidExterminator());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("A departure after the end step begins does not retroactively trigger Disappear")
    void departureDuringEndStepDoesNotTrigger() {
        harness.addToBattlefield(player1, new InsectoidExterminator());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, land));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
