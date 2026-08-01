package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.action.ExileToOwnerGraveyardAtNextEndStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElkinLairTest extends BaseCardTest {

    private StepTriggerService stepTriggerService() {
        return GameTestEngineContext.get().getBean(StepTriggerService.class);
    }

    @Test
    @DisplayName("Active player's upkeep exiles a random hand card with this-turn play permission")
    void upkeepExilesRandomHandCardWithPlayPermission() {
        harness.addToBattlefield(player1, new ElkinLair());
        Card only = new Shock();
        harness.setHand(player1, List.of(only));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(only.getId()));
        assertThat(gd.exilePlayPermissions.get(only.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(only.getId());

        List<ExileToOwnerGraveyardAtNextEndStep> scheduled =
                gd.getDelayedActions(ExileToOwnerGraveyardAtNextEndStep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().cardId()).isEqualTo(only.getId());
        assertThat(scheduled.getFirst().ownerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Opponent's upkeep also exiles from that player's hand")
    void opponentUpkeepExilesFromOpponentHand() {
        harness.addToBattlefield(player1, new ElkinLair());
        Card only = new GrizzlyBears();
        harness.setHand(player2, List.of(only));
        harness.setHand(player1, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(only.getId()));
        assertThat(gd.exilePlayPermissions.get(only.getId())).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Empty hand is a no-op")
    void emptyHandDoesNothing() {
        harness.addToBattlefield(player1, new ElkinLair());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextEndStep.class)).isEmpty();
    }

    @Test
    @DisplayName("Unplayed card goes to graveyard at next end step")
    void unplayedCardGoesToGraveyardAtEndStep() {
        harness.addToBattlefield(player1, new ElkinLair());
        Card only = new Shock();
        harness.setHand(player1, List.of(only));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.inMutationScope(() -> stepTriggerService().handleEndStepTriggers(gd));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(only.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(only.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(only.getId()));
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextEndStep.class)).isEmpty();
    }

    @Test
    @DisplayName("A played card is not put into the graveyard at end step")
    void playedCardIsNotPutIntoGraveyard() {
        harness.addToBattlefield(player1, new ElkinLair());
        Card land = new Mountain();
        harness.setHand(player1, List.of(land));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        gs.playCardFromExile(gd, player1, land.getId(), null, null);
        harness.assertOnBattlefield(player1, "Mountain");

        harness.inMutationScope(() -> stepTriggerService().handleEndStepTriggers(gd));

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.playerGraveyards.getOrDefault(player1.getId(), List.of()))
                .noneMatch(c -> c.getId().equals(land.getId()));
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextEndStep.class)).isEmpty();
    }

    @Test
    @DisplayName("Exiled spell can be cast this turn for its mana cost")
    void mayCastExiledSpellThisTurn() {
        harness.addToBattlefield(player1, new ElkinLair());
        Card shock = new Shock();
        harness.setHand(player1, List.of(shock));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        var bears = findPermanent(player2, "Grizzly Bears");

        gs.playCardFromExile(gd, player1, shock.getId(), null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(shock.getId()));
    }
}
