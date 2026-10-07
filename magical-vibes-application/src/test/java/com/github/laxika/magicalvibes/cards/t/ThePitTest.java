package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AssaultSuit;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThePit.class, GrizzlyBears.class, Ornithopter.class, AssaultSuit.class})
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

    @Test
    @DisplayName("Arrival waits for all choices before creating any tokens")
    void arrivalCreatesTokensOnlyAfterAllChoices() {
        triggerPlaneswalkTo();
        harness.handleListChoice(player1, DEMON);

        assertThat(findPermanents(player1, "Demon")).isEmpty();
        assertThat(findPermanents(player2, "Angel")).isEmpty();

        harness.handleListChoice(player2, ANGEL);

        assertThat(findPermanents(player1, "Demon")).hasSize(1);
        assertThat(findPermanents(player2, "Angel")).hasSize(1);
    }

    @Test
    @DisplayName("Arrival choices start with the active player even in the second seat")
    void arrivalChoicesFollowActivePlayerOrder() {
        harness.forceActivePlayer(player2);
        triggerPlaneswalkTo();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, DEMON);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, ANGEL);

        assertThat(findPermanents(player1, "Angel")).hasSize(1);
        assertThat(findPermanents(player2, "Demon")).hasSize(1);
    }

    @Test
    @DisplayName("A Demon sacrifices another creature, including an artifact creature, instead of dealing damage")
    void demonSacrificesOtherCreatureWithoutDamage() {
        triggerPlaneswalkTo();
        harness.handleListChoice(player1, DEMON);
        harness.handleListChoice(player2, ANGEL);
        Permanent otherCreature = addCreatureReady(player1, new Ornithopter());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCreature.getCard());
        assertThat(findPermanents(player1, "Demon")).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The Demon controller chooses another creature when several are available")
    void demonControllerChoosesAnotherCreature() {
        triggerPlaneswalkTo();
        harness.handleListChoice(player1, DEMON);
        harness.handleListChoice(player2, ANGEL);
        Permanent demon = findPermanent(player1, "Demon");
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent thopter = addCreatureReady(player1, new Ornithopter());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), thopter.getId());
        harness.handlePermanentChosen(player1, thopter.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(demon, bears).doesNotContain(thopter);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(thopter.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("A second-seat Demon triggers only during its controller's upkeep")
    void demonTriggersOnlyDuringItsControllersUpkeep() {
        triggerPlaneswalkTo();
        harness.handleListChoice(player1, ANGEL);
        harness.handleListChoice(player2, DEMON);
        int firstLife = gd.playerLifeTotals.get(player1.getId());
        int secondLife = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(firstLife);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(secondLife);

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(firstLife);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(secondLife - 6);
        assertThat(findPermanents(player2, "Demon")).hasSize(1);
        assertThat(findPermanents(player1, "Angel")).hasSize(1);
    }

    @Test
    @DisplayName("A Demon deals damage when its only other creature cannot be sacrificed")
    void demonCannotSacrificeCreatureProtectedByAssaultSuit() {
        triggerPlaneswalkTo();
        harness.handleListChoice(player1, DEMON);
        harness.handleListChoice(player2, ANGEL);
        Permanent otherCreature = addCreatureReady(player1, new Ornithopter());
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new AssaultSuit());
        suit.setAttachedTo(otherCreature.getId());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherCreature, suit);
        assertThat(findPermanents(player1, "Demon")).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    @DisplayName("Chaos skips a player whose only creature is an artifact")
    void chaosSkipsPlayerWithoutNonartifactCreatures() {
        Permanent artifactCreature = addCreatureReady(player1, new Ornithopter());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.trigger(gd, source, EffectSlot.CHAOS_TRIGGERED,
                player1.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifactCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Chaos collects each player's choice before sacrificing the chosen creatures")
    void chaosWaitsForBothPlayersSacrificeChoices() {
        Permanent firstBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstOtherBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent artifactCreature = addCreatureReady(player1, new Ornithopter());
        Permanent secondBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondOtherBears = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.trigger(gd, source, EffectSlot.CHAOS_TRIGGERED,
                player1.getId()));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(firstBears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(firstBears, firstOtherBears, artifactCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondBears, secondOtherBears);
        harness.handleMultiplePermanentsChosen(player2, List.of(secondOtherBears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(firstOtherBears, artifactCreature).doesNotContain(firstBears);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(secondBears).doesNotContain(secondOtherBears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstBears.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondOtherBears.getCard());
    }

    private void triggerPlaneswalkTo() {
        harness.inMutationScope(() -> planar.trigger(gd, source, EffectSlot.PLANESWALK_TO_TRIGGERED,
                player1.getId()));
        harness.passBothPriorities();
    }
}
