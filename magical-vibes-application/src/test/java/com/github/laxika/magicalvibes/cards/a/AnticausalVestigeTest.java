package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnticausalVestige.class, Forest.class, GrizzlyBears.class})
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
        harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, vestige));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof Forest);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        int bearsIndex = gd.playerHands.get(player1.getId()).indexOf(bears);
        harness.handleCardChosen(player1, bearsIndex);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(bears.getId()) && permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
    }
}
