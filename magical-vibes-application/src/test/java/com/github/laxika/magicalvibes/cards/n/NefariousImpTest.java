package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MinimusContainment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NefariousImp.class, Forest.class, GrizzlyBears.class, MinimusContainment.class})
class NefariousImpTest extends BaseCardTest {

    @Test
    @DisplayName("Scries 1 when a permanent you control leaves the battlefield")
    void scriesWhenOwnPermanentLeaves() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new NefariousImp());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Triggers when Nefarious Imp itself leaves the battlefield")
    void scriesWhenSelfLeaves() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new NefariousImp());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, imp));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    @DisplayName("Triggers only once when multiple controlled permanents leave together")
    void triggersOnceForSimultaneousLeaves() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player1, new NefariousImp());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().beginPermanentLeaveBatch(gd);
            try {
                harness.getPermanentRemovalService().removePermanentToHand(gd, first);
                harness.getPermanentRemovalService().removePermanentToHand(gd, second);
            } finally {
                harness.getPermanentRemovalService().endPermanentLeaveBatch(gd);
            }
        });
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's permanent leaving does not trigger")
    void opponentPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new NefariousImp());
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, opponentPermanent));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void scriesWhenControlledLandLeavesAndCanBottomTheCard() {
        Forest top = new Forest();
        NefariousImp next = new NefariousImp();
        harness.setLibrary(player1, List.of(top, next));
        harness.addToBattlefield(player1, new NefariousImp());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, land));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
    }

    @Test
    void separateLeaveEventsEachTrigger() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new NefariousImp());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, first));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, second));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scriesOnceWhenSelfAndLandLeaveTogether() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new NefariousImp());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().beginPermanentLeaveBatch(gd);
            try {
                harness.getPermanentRemovalService().removePermanentToHand(gd, imp);
                harness.getPermanentRemovalService().removePermanentToHand(gd, land);
            } finally {
                harness.getPermanentRemovalService().endPermanentLeaveBatch(gd);
            }
        });
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scriesWhenSelfDies() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new NefariousImp());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, imp));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nefarious Imp");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    void resolvesScryWithAnEmptyLibraryWithoutOpeningAnInteraction() {
        harness.setLibrary(player1, List.of());
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new NefariousImp());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, imp));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotScryWhileMinimusContainmentRemovesItsAbility() {
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new NefariousImp());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new MinimusContainment());
        aura.setAttachedTo(imp.getId());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, land));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
}
