package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({BoneyardLurker.class, GrizzlyBears.class, HolyDay.class, Forest.class})
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
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
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

    @Test
    @DisplayName("Mutating can return a land card, while excluding instants from target choices")
    void mutatingReturnsLandCard() {
        Permanent lurker = addCreatureReady(player1, new BoneyardLurker());
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land, new HolyDay()));

        triggerMutation(lurker);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(land.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("A target leaving the graveyard before resolution is not returned or replaced")
    void missingTargetIsNotReplacedAtResolution() {
        Permanent lurker = addCreatureReady(player1, new BoneyardLurker());
        Card target = new GrizzlyBears();
        Card other = new Forest();
        harness.setGraveyard(player1, List.of(target, other));

        triggerMutation(lurker);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another creature mutating does not trigger Boneyard Lurker")
    void anotherCreatureMutatingDoesNotTrigger() {
        addCreatureReady(player1, new BoneyardLurker());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest()));

        triggerMutation(other);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    private void triggerMutation(Permanent lurker) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, lurker, List.of(lurker.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
