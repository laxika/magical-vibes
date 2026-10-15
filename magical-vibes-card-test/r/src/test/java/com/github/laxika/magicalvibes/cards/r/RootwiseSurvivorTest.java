package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootwiseSurvivor.class, Forest.class})
class RootwiseSurvivorTest extends BaseCardTest {

    @Test
    void tappedSurvivorEarthbendsLandAtPostcombatMain() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        survivor.tap();

        advanceToPostcombatMain(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void untappedSurvivorDoesNotTrigger() {
        harness.addToBattlefield(player1, new RootwiseSurvivor());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void earthbendTargetMustBeLandControlledBySurvivorController() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        survivor.tap();

        advanceToPostcombatMain(player1);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownForest.getId(), player1.getId())
                .doesNotContain(opponentForest.getId());
        harness.handlePermanentChosen(player1, ownForest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, opponentForest)).isTrue();
        assertThat(gqs.isCreature(gd, opponentForest)).isFalse();
        assertThat(opponentForest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mayDeclineTargetingLand() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        survivor.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void animatedLandGainsElementalSubtypeAndKeepsForestSubtype() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        survivor.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.FOREST)).isTrue();
    }

    @Test
    void landLosesGrantedHasteAtControllersNextTurnButRemainsAnimated() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        survivor.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void animatedLandStaysInGraveyardAfterDying() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        survivor.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        forest.setMarkedDamage(3);
        harness.runStateBasedActions();
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void untappingSurvivorBeforeResolutionPreventsAnimationAndCounters() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        survivor.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, forest.getId());
        survivor.untap();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void survivalDoesNotTriggerDuringOpponentsSecondMainPhase() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        survivor.tap();

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void survivalCanResolveWithoutAnyLandToTarget() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        survivor.tap();

        advanceToPostcombatMain(player1);
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Rootwise Survivor");
    }

    @Test
    void animatingSameLandAgainAddsCountersWithoutResettingThem() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        survivor.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        survivor.tap();
        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(6);
    }

    @Test
    void animatedLandStaysExiledWithoutCreatingReturnTrigger() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new RootwiseSurvivor());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        survivor.tap();

        advanceToPostcombatMain(player1);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToExile(gd, forest));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(forest.getOriginalCard().getId())).isNotNull();
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
    }
}
