package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Delay;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TalonGates.class, AncestralVision.class, GrizzlyBears.class, Forest.class, Delay.class})
class TalonGatesTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        source = new PlanarObject(new TalonGates(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void exilesChosenNonlandCardWithItsManaValueAsTimeCounters() {
        Card card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));

        gs.activatePlanarAbility(
                gd, player1, source.getId(), 0, null, null, null);

        PendingInteraction.PlanarAbilityHandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PlanarAbilityHandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chaosRemovesTwoTimeCountersOnlyFromCardsIOwn() {
        AncestralVision own = suspendedCard(player1, 4);
        AncestralVision opponent = suspendedCard(player2, 4);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(own.getId(), 2)
                .containsEntry(opponent.getId(), 4);
    }

    @Test
    void nonlandChoiceExcludesLands() {
        Forest land = new Forest();
        GrizzlyBears bear = new GrizzlyBears();
        harness.setHand(player1, List.of(land, bear));

        gs.activatePlanarAbility(gd, player1, source.getId(), 0, null, null, null);

        PendingInteraction.PlanarAbilityHandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PlanarAbilityHandCardChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bear);
    }

    @Test
    void exileActionCannotBeTakenOutsideMainPhase() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> gs.activatePlanarAbility(
                gd, player1, source.getId(), 0, null, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void specialActionIsAllowedWhenActivatedAbilitiesAreProhibited() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.setHand(player1, List.of(bear));
        gd.playersCantActivateAbilitiesThisTurn.add(player1.getId());

        gs.activatePlanarAbility(gd, player1, source.getId(), 0, null, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bear);
        assertThat(gd.exiledCardTimeCounters).containsEntry(bear.getId(), 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void zeroManaValueCardRemainsExiledWithoutACastTrigger() {
        AncestralVision card = new AncestralVision();
        harness.setHand(player1, List.of(card));
        gs.activatePlanarAbility(gd, player1, source.getId(), 0, null, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedSuspendContinuesAfterLeavingPlaneAndCastsCreatureWithHaste() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.setHand(player1, List.of(bear));
        gs.activatePlanarAbility(gd, player1, source.getId(), 0, null, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        gd.planechase.faceUp.clear();

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(bear.getId(), 2);
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(bear.getId(), 1);
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bear);
        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void chaosRemovesLastRemainingCounterAndAllowsDecliningTheCast() {
        AncestralVision card = suspendedCard(player1, 1);
        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void chaosAlsoRemovesCountersFromCardsSuspendedByDelay() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.castFromHand(player1, bear, "{1}{G}");
        harness.setHand(player2, List.of(new Delay()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bear.getId());

        harness.inMutationScope(() -> planar.chaos(gd));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(bear);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(bear.getId(), player1.getId(), 1));
    }

    private AncestralVision suspendedCard(com.github.laxika.magicalvibes.model.Player owner,
                                          int timeCounters) {
        AncestralVision card = new AncestralVision();
        harness.setExile(owner, List.of(card));
        gd.exiledCardTimeCounters.put(card.getId(), timeCounters);
        return card;
    }
}
