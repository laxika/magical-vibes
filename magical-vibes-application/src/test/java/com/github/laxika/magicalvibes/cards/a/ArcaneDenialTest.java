package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArcaneDenialTest extends BaseCardTest {

    /** player1 casts Grizzly Bears, player2 counters it with Arcane Denial. */
    private GrizzlyBears counterBears() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ArcaneDenial()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        return bears;
    }

    private void runUpkeep() {
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
    }

    @Test
    @DisplayName("Counters the spell and schedules both delayed draws — nobody draws immediately")
    void countersAndSchedulesDraws() {
        counterBears();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(2);
        assertThat(scheduled).anySatisfy(a -> {
            assertThat(a.controllerId()).isEqualTo(player1.getId());
            assertThat(a.count()).isEqualTo(2);
            assertThat(a.upTo()).isTrue();
        });
        assertThat(scheduled).anySatisfy(a -> {
            assertThat(a.controllerId()).isEqualTo(player2.getId());
            assertThat(a.count()).isEqualTo(1);
            assertThat(a.upTo()).isFalse();
        });
    }

    @Test
    @DisplayName("At the next upkeep the caster draws one and the countered spell's controller may draw two")
    void upkeepDraws() {
        counterBears();
        runUpkeep();

        // The caster's draw is automatic; the countered spell's controller chooses.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("The countered spell's controller may decline to draw")
    void mayDeclineToDraw() {
        counterBears();
        runUpkeep();
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
