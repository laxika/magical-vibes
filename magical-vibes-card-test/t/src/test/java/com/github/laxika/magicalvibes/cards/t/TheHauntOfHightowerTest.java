package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheHauntOfHightower.class, GrizzlyBears.class})
class TheHauntOfHightowerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes the defending player discard a card")
    void attackingMakesDefendingPlayerDiscard() {
        Permanent haunt = addCreatureReady(player1, new TheHauntOfHightower());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(haunt)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        resolveAllTriggers();
        assertThat(haunt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when a card is put into an opponent's graveyard")
    void getsCounterForOpponentCardPutIntoGraveyard() {
        Permanent haunt = harness.addToBattlefieldAndReturn(player1, new TheHauntOfHightower());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, opponentCreature));
        resolveAllTriggers();

        assertThat(haunt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when a card is put into its controller's graveyard")
    void doesNotTriggerForControllerGraveyard() {
        Permanent haunt = harness.addToBattlefieldAndReturn(player1, new TheHauntOfHightower());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ownCreature));
        resolveAllTriggers();

        assertThat(haunt.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
