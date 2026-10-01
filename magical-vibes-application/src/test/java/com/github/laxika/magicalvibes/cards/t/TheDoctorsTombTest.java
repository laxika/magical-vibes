package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheDoctorsTomb.class, GrizzlyBears.class})
class TheDoctorsTombTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TheDoctorsTomb(), gd.nextTimestamp()));
    }

    @Test
    void exilesDyingCreaturesAndTheirControllersLoseTwoLife() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 10);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));

        assertThat(gd.findExiledCard(creature.getCard().getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(creature.getCard().getId()));
        harness.assertLife(player2, 8);
    }

    @Test
    void chaosRedistributesWholeLifeTotals() {
        harness.setLife(player1, 5);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Alice: 20; Bob: 5");
        harness.handleListChoice(player1, "Alice: 20; Bob: 5");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 5);
    }
}
