package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.IntrepidTenderfoot;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WordsOfWind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnticausalVestige.class, Forest.class, IntrepidTenderfoot.class, WordsOfWind.class})
class AnticausalVestigeTest extends BaseCardTest {

    @Test
    void warpExilesAtTheNextEndStepAndGrantsPermissionOnTheNextTurn() {
        AnticausalVestige vestige = new AnticausalVestige();
        harness.setHand(player1, List.of(vestige));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(vestige.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Anticausal Vestige");
        assertThat(gd.findExiledCard(vestige.getId())).isNull();
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();
        assertThat(gd.findExiledCard(vestige.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(vestige.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).containsEntry(vestige.getId(), player1.getId());

        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).containsEntry(vestige.getId(), player1.getId());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castFromExile(player1, vestige.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(vestige.getId()));
    }

    @Test
    void leavesBattlefieldAbilityDrawsThenOffersPermanentUpToLandCountTapped() {
        Permanent vestige = addCreatureReady(player1, new AnticausalVestige());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        IntrepidTenderfoot creature = new IntrepidTenderfoot();
        harness.setHand(player1, List.of(creature));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, vestige));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof Forest);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(creature));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()) && permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    void newlyDrawnLandCanEnterTappedWithNoLandsControlled() {
        Permanent vestige = addCreatureReady(player1, new AnticausalVestige());
        Forest drawnLand = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnLand));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, vestige));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnLand);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningPermanentPlacementStillDrawsACard() {
        Permanent vestige = addCreatureReady(player1, new AnticausalVestige());
        Forest drawnLand = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnLand));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, vestige));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentLandsDoNotIncreaseTheManaValueLimit() {
        Permanent vestige = addCreatureReady(player1, new AnticausalVestige());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        IntrepidTenderfoot creature = new IntrepidTenderfoot();
        Forest drawnLand = new Forest();
        harness.setHand(player1, List.of(creature));
        harness.setLibrary(player1, List.of(drawnLand));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, vestige));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void landCountIsDeterminedAfterTheDrawReplacementReturnsALand() {
        harness.addToBattlefield(player1, new WordsOfWind());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent vestige = addCreatureReady(player1, new AnticausalVestige());
        IntrepidTenderfoot creature = new IntrepidTenderfoot();
        Forest undrawnLand = new Forest();
        harness.setHand(player1, List.of(creature));
        harness.setLibrary(player1, List.of(undrawnLand));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, vestige));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(opposingLand.getId()));
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawnLand);
    }
}
