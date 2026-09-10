package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FortifiedVillage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheAetherFlues.class, Forest.class, FortifiedVillage.class, GrizzlyBears.class, Island.class})
class TheAetherFluesTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TheAetherFlues(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }

    @Test
    void upkeepSacrificeFindsCreatureAndShufflesOtherRevealedCardsIntoLibrary() {
        Card sacrificed = new GrizzlyBears();
        Card found = new GrizzlyBears();
        Card forest = new Forest();
        Card island = new Island();
        harness.addToBattlefield(player1, sacrificed);
        harness.setLibrary(player1, List.of(forest, found, island));

        triggerUpkeep();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())).contains(found);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, island);
    }

    @Test
    void planeswalkingToThePlaneTriggersTheSacrificeAbility() {
        Card sacrificed = new GrizzlyBears();
        Card found = new GrizzlyBears();
        harness.addToBattlefield(player1, sacrificed);
        harness.setLibrary(player1, List.of(found));
        gd.planechase.deck.addFirst(new TheAetherFlues());

        harness.inMutationScope(() -> planar.planeswalk(gd));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())).contains(found);
    }

    @Test
    void decliningUpkeepSacrificeDoesNothing() {
        Card sacrificed = new GrizzlyBears();
        Card forest = new Forest();
        Card found = new GrizzlyBears();
        harness.addToBattlefield(player1, sacrificed);
        harness.setLibrary(player1, List.of(forest, found));

        triggerUpkeep();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())).contains(sacrificed).doesNotContain(found);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, found);
    }

    @Test
    void chaosMayPutCreatureFromHandOntoBattlefield() {
        Card creature = new GrizzlyBears();
        Card land = new FortifiedVillage();
        harness.setHand(player1, List.of(creature, land));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())).contains(creature);
    }

    @Test
    void decliningChaosLeavesHandUnchanged() {
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())).doesNotContain(creature);
    }

    private void triggerUpkeep() {
        StepTriggerService steps = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> steps.handleUpkeepTriggers(gd));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
