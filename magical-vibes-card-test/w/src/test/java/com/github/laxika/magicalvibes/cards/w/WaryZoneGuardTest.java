package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaryZoneGuard.class, Forest.class, GrizzlyBears.class})
class WaryZoneGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped; survival returns a land card and it perpetually gets +1/+1")
    void entersTappedAndSurvivalReturnsLandAndPerpetuallyBoostsGuard() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        Card forest = new Forest();
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear, forest));

        assertThat(guard.isTapped()).isTrue();

        advanceToPostcombatMain(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(forest.getId());

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId())
                        && !permanent.isTapped());
    }

    @Test
    @DisplayName("With no land card in the graveyard, survival still boosts it")
    void survivalCanBeDeclinedWhenNoLandIsInGraveyard() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(4);
    }

    @Test
    @DisplayName("An untapped Guard doesn't trigger survival")
    void untappedGuardDoesNotTriggerSurvival() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        guard.untap();
        harness.setGraveyard(player1, List.of(new Forest()));

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Untapping before survival resolves stops both the return and the growth")
    void untappingBeforeResolutionPreventsReturnAndGrowth() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        guard.untap();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(forest.getId()));
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Choosing no land target still boosts it")
    void choosingNoTargetStillBoostsGuardWhenLandIsAvailable() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's second main phase doesn't trigger survival")
    void opponentsSecondMainPhaseDoesNotTriggerSurvival() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        harness.setGraveyard(player1, List.of(new Forest()));

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing the only target stops both the return and the growth")
    void removingTheOnlyTargetPreventsBothReturnAndGrowth() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(forest));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Survival returns only a land card from its controller's graveyard")
    void survivalReturnsOnlyControllersLand() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        Card ownForest = new Forest();
        Card opposingForest = new Forest();
        harness.setGraveyard(player1, List.of(ownForest));
        harness.setGraveyard(player2, List.of(opposingForest));

        advanceToPostcombatMain(player1);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownForest.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownForest.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingForest);
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(4);
    }

    @Test
    @DisplayName("The growth stays after it returns to hand and is cast again")
    void perpetualGrowthSurvivesReturningToHandAndRecasting() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, guard));
        Card returnedGuard = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(guard.getCard().getId())).findFirst().orElseThrow();
        harness.castFromHand(player1, returnedGuard, "{2}{G}");
        harness.passBothPriorities();

        Permanent recastGuard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(returnedGuard.getId()))
                .findFirst().orElseThrow();
        assertThat(recastGuard.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, recastGuard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, recastGuard)).isEqualTo(4);
    }

    @Test
    @DisplayName("Returned to hand before survival resolves, the land still returns but the card isn't boosted (CR 400.7)")
    void returningTappedGuardToHandBeforeResolutionStillReturnsLandButDoesNotBoostTheCard() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, guard));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        Card returnedGuard = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(guard.getCard().getId())).findFirst().orElseThrow();
        harness.castFromHand(player1, returnedGuard, "{2}{G}");
        harness.passBothPriorities();
        Permanent recastGuard = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(returnedGuard.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, recastGuard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recastGuard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Dying before survival resolves, the land still returns but the card isn't boosted (CR 400.7)")
    void dyingTappedBeforeResolutionStillReturnsLandButDoesNotBoostTheCard() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, guard));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        Card deadGuard = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(guard.getCard().getId())).findFirst().orElseThrow();
        harness.setGraveyard(player1, List.of());
        Permanent returnedGuard = harness.enterBattlefieldAndReturn(player1, deadGuard);
        assertThat(gqs.getEffectivePower(gd, returnedGuard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returnedGuard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Survival doesn't trigger in a later main phase of the same turn")
    void survivalDoesNotTriggerDuringLaterMainPhaseOfSameTurn() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        gd.mainPhasesBegunThisTurn = 2;

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Survival triggers in the second main phase after an additional combat")
    void survivalTriggersInTheSecondMainPhaseAfterAnAdditionalCombat() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        gd.combatPhasesThisTurn = 2;

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(4);
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
