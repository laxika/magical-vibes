package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThePit.class, GrizzlyBears.class, Ornithopter.class})
class ThePitTest extends BaseCardTest {

    private static final String ANGEL = "Create a 3/3 white Angel token";
    private static final String DEMON = "Create a 6/6 black Demon token";

    private PlanechaseService planar;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        source = new PlanarObject(new ThePit(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Each player independently chooses an Angel or Demon token on arrival")
    void arrivalLetsEachPlayerChooseTheirToken() {
        triggerPlaneswalkTo();

        PendingInteraction.ColorChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.options()).containsExactly(ANGEL, DEMON);
        harness.handleListChoice(player1, DEMON);

        PendingInteraction.ColorChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ANGEL);

        assertThat(findPermanents(player1, "Demon")).hasSize(1);
        assertThat(findPermanents(player2, "Angel")).hasSize(1);
    }

    @Test
    @DisplayName("A Demon token deals 6 damage to its controller when no other creature can be sacrificed")
    void demonUpkeepDealsDamageIfNoOtherCreatureExists() {
        triggerPlaneswalkTo();
        harness.handleListChoice(player1, DEMON);
        harness.handleListChoice(player2, ANGEL);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    @DisplayName("Chaos makes each player sacrifice a nonartifact creature")
    void chaosSacrificesOneNonartifactCreaturePerPlayer() {
        Permanent creature1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent artifactCreature = addCreatureReady(player1, new Ornithopter());
        Permanent creature2 = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.trigger(gd, source, EffectSlot.CHAOS_TRIGGERED,
                player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifactCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature1.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature2.getCard());
    }

    private void triggerPlaneswalkTo() {
        harness.inMutationScope(() -> planar.trigger(gd, source, EffectSlot.PLANESWALK_TO_TRIGGERED,
                player1.getId()));
        harness.passBothPriorities();
    }
}
