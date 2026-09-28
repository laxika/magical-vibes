package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MountVelusManticore.class, GrizzlyBears.class, SolemnSimulacrum.class})
class MountVelusManticoreTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a creature card deals one damage to a target")
    void discardOneTypeCardDealsOneDamage() {
        harness.addToBattlefield(player1, new MountVelusManticore());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        triggerAtBeginningOfCombat(player1);
        resolveMayAndDiscard(0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Discarding an artifact creature card deals two damage to a target")
    void discardTwoTypeCardDealsTwoDamage() {
        harness.addToBattlefield(player1, new MountVelusManticore());
        harness.setHand(player1, new ArrayList<>(List.of(new SolemnSimulacrum())));

        triggerAtBeginningOfCombat(player1);
        resolveMayAndDiscard(0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the may choice does not discard or deal damage")
    void declineDoesNothing() {
        harness.addToBattlefield(player1, new MountVelusManticore());
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(card)));

        triggerAtBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new MountVelusManticore());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        triggerAtBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void triggerAtBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void resolveMayAndDiscard(int handIndex) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, handIndex);
    }
}
