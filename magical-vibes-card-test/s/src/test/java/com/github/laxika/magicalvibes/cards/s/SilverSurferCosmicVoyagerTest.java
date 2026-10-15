package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilverSurferCosmicVoyager.class, GrizzlyBears.class, Forest.class})
class SilverSurferCosmicVoyagerTest extends BaseCardTest {

    @Test
    void exilesAnyNumberOfOtherPermanentsAndReturnsLandsTappedAtNextEndStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new SilverSurferCosmicVoyager(), "{4}{U}{U}");
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(bears.getId(), forest.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(opponentBears.getId(),
                harness.getPermanentId(player1, "Silver Surfer, Cosmic Voyager"));

        harness.handlePermanentChosen(player1, bears.getId());
        choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(forest.getId());
        assertThat(choice.validPlayerIds()).contains(player1.getId());
        harness.handlePermanentChosen(player1, forest.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        Permanent returnedForest = findPermanent(player1, "Forest");
        assertThat(returnedBears.isTapped()).isFalse();
        assertThat(returnedForest.isTapped()).isTrue();
    }

    @Test
    void mayChooseNoPermanents() {
        harness.castFromHand(player1, new SilverSurferCosmicVoyager(), "{4}{U}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
        harness.assertOnBattlefield(player1, "Silver Surfer, Cosmic Voyager");
    }

    @Test
    void mayDeclineAllTargetsEvenWhenOtherPermanentsAreAvailable() {
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new SilverSurferCosmicVoyager(), "{4}{U}{U}");
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    @Test
    void canStopAfterChoosingOnlySomeEligiblePermanents() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new SilverSurferCosmicVoyager(), "{4}{U}{U}");
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    @Test
    void permanentsWithDifferentOwnersReturnTogetherFromOneDelayedAbility() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        GrizzlyBears borrowedCard = new GrizzlyBears();
        borrowedCard.setOwnerId(player2.getId());
        Permanent borrowedBears = harness.addToBattlefieldAndReturn(player1, borrowedCard);
        gd.stolenCreatures.put(borrowedBears.getId(), player2.getId());

        harness.castFromHand(player1, new SilverSurferCosmicVoyager(), "{4}{U}{U}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, forest.getId());
        harness.handlePermanentChosen(player1, borrowedBears.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayTargetMoreThanNinetyNineOtherPermanents() {
        List<Permanent> forests = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            forests.add(harness.addToBattlefieldAndReturn(player1, new Forest()));
        }
        harness.castFromHand(player1, new SilverSurferCosmicVoyager(), "{4}{U}{U}");
        resolveAllTriggers();

        for (Permanent forest : forests) {
            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validPermanentIds()).contains(forest.getId());
            harness.handlePermanentChosen(player1, forest.getId());
        }
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(100);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);
        assertThat(findPermanents(player1, "Forest")).hasSize(100).allMatch(Permanent::isTapped);
    }

    @Test
    void enteringDuringEndStepWaitsForFollowingTurnsEndStep() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.castFromHand(player1, new SilverSurferCosmicVoyager(), "{4}{U}{U}");
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    void abilityStillResolvesAfterSilverSurferLeavesTheBattlefield() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new SilverSurferCosmicVoyager(), "{4}{U}{U}");
        resolveAllTriggers();
        Permanent surfer = findPermanent(player1, "Silver Surfer, Cosmic Voyager");
        harness.handlePermanentChosen(player1, forest.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, surfer));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Silver Surfer, Cosmic Voyager");
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    void returnsRemainingLegalTargetsWhenAnotherTargetLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new SilverSurferCosmicVoyager(), "{4}{U}{U}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, forest.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotReturnCardThatLeavesExileAndIsExiledAgainBeforeEndStep() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new SilverSurferCosmicVoyager(), "{4}{U}{U}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, forest.getId());
        resolveAllTriggers();

        assertThat(gd.removeFromExile(forest.getCard().getId())).isTrue();
        gd.addCardToHand(player1.getId(), forest.getCard());
        gd.playerHands.get(player1.getId()).remove(forest.getCard());
        gd.addToExile(player1.getId(), forest.getCard());

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(forest.getCard().getId())).isNotNull();
    }
}
