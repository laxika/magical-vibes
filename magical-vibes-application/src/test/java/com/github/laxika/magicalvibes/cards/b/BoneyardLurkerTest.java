package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneyardLurker.class, GrizzlyBears.class, HolyDay.class})
class BoneyardLurkerTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating returns a target permanent card from its controller's graveyard to hand")
    void mutatingReturnsTargetPermanentCardToHand() {
        Permanent lurker = addCreatureReady(player1, new BoneyardLurker());
        Card permanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(permanent));

        triggerMutation(lurker);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(permanent.getId());

        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(permanent.getId()));
    }

    @Test
    @DisplayName("Mutating cannot target a nonpermanent card")
    void mutatingCannotTargetNonpermanentCard() {
        Permanent lurker = addCreatureReady(player1, new BoneyardLurker());
        Card nonpermanent = new HolyDay();
        harness.setGraveyard(player1, List.of(nonpermanent));

        triggerMutation(lurker);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("Mutating cannot target a permanent card in an opponent's graveyard")
    void mutatingCannotTargetOpponentGraveyard() {
        Permanent lurker = addCreatureReady(player1, new BoneyardLurker());
        Card opponentPermanent = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(opponentPermanent));

        triggerMutation(lurker);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void triggerMutation(Permanent lurker) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, lurker, List.of(lurker.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
