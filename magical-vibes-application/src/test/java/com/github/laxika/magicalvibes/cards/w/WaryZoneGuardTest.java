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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaryZoneGuard.class, Forest.class, GrizzlyBears.class})
class WaryZoneGuardTest extends BaseCardTest {

    @Test
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
    void survivalCanBeDeclinedWhenNoLandIsInGraveyard() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());

        advanceToPostcombatMain(player1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(4);
    }

    @Test
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
    void opponentsSecondMainPhaseDoesNotTriggerSurvival() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        harness.setGraveyard(player1, List.of(new Forest()));

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(3);
    }

    @Test
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
    void returningTappedGuardToHandBeforeResolutionStillReturnsLandAndPerpetuallyBoostsCard() {
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
        assertThat(gqs.getEffectivePower(gd, recastGuard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, recastGuard)).isEqualTo(4);
    }

    @Test
    void dyingTappedBeforeResolutionStillReturnsLandAndPerpetuallyBoostsCard() {
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
        assertThat(gqs.getEffectivePower(gd, returnedGuard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returnedGuard)).isEqualTo(4);
    }

    @Test
    void survivalDoesNotTriggerDuringLaterMainPhaseOfSameTurn() {
        Permanent guard = harness.enterBattlefieldAndReturn(player1, new WaryZoneGuard());
        gd.combatPhasesThisTurn = 2;

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(3);
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
