package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({TheAetherFlues.class, Forest.class, GrizzlyBears.class, Island.class, PsychogenicProbe.class})
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
    @DisplayName("Sacrificing a creature at upkeep puts the first revealed creature onto the battlefield and shuffles the rest in")
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
    @DisplayName("Planeswalking to the plane triggers the sacrifice ability")
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
    @DisplayName("Declining the upkeep sacrifice reveals nothing")
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
    @DisplayName("Chaos lets the controller put a creature card from hand onto the battlefield")
    void chaosMayPutCreatureFromHandOntoBattlefield() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
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
    @DisplayName("Declining the chaos ability leaves the hand unchanged")
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

    @Test
    @DisplayName("The sacrifice and the reveal happen in the same resolution")
    void sacrificeAndRevealResolveWithoutAnotherPriorityRound() {
        Card sacrificed = new GrizzlyBears();
        Card found = new GrizzlyBears();
        harness.addToBattlefield(player1, sacrificed);
        harness.setLibrary(player1, List.of(found));

        triggerUpkeep();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())).contains(found);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Without a creature to sacrifice, the sacrifice isn't offered and the library is unchanged")
    void noCreatureToSacrificeLeavesLibraryUnchanged() {
        Card found = new GrizzlyBears();
        harness.setLibrary(player1, List.of(found));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(found);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A library without creatures is revealed and shuffled back in full")
    void libraryWithoutCreaturesIsReturnedInFull() {
        Card sacrificed = new GrizzlyBears();
        Card forest = new Forest();
        Card island = new Island();
        harness.addToBattlefield(player1, sacrificed);
        harness.setLibrary(player1, List.of(forest, island));

        triggerUpkeep();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, island);
    }

    @Test
    @DisplayName("Finding a creature as the first card still shuffles the library")
    void findingCreatureFirstStillShufflesLibrary() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card found = new GrizzlyBears();
        harness.setLibrary(player1, List.of(found, new Forest(), new Island()));
        harness.setLife(player1, 20);

        triggerUpkeep();
        harness.handleMayAbilityChosen(player1, true);
        for (int i = 0; i < 2 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("An empty library is still shuffled after the sacrifice")
    void emptyLibraryStillShufflesAfterSacrifice() {
        harness.addToBattlefield(player1, new PsychogenicProbe());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        triggerUpkeep();
        harness.handleMayAbilityChosen(player1, true);
        for (int i = 0; i < 2 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Chaos with only lands in hand puts nothing onto the battlefield")
    void chaosWithOnlyLandsInHandPutsNothingOntoBattlefield() {
        Card land = new Forest();
        harness.setHand(player1, List.of(land));

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The upkeep ability uses the active planar controller's creatures and library")
    void upkeepUsesActivePlayersCreaturesAndLibrary() {
        Card opponentCreature = new GrizzlyBears();
        Card sacrificed = new GrizzlyBears();
        Card found = new GrizzlyBears();
        Card untouched = new Forest();
        harness.addToBattlefield(player1, opponentCreature);
        harness.addToBattlefield(player2, sacrificed);
        harness.setLibrary(player1, List.of(untouched));
        harness.setLibrary(player2, List.of(found));
        harness.forceActivePlayer(player2);

        triggerUpkeep();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(sacrificed);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .map(permanent -> permanent.getCard())).contains(found);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard())).contains(opponentCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }

    private void triggerUpkeep() {
        advanceToUpkeep(gd.activePlayerId.equals(player2.getId()) ? player2 : player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
