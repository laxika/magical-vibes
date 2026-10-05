package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RiftSower;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MountVelusManticore.class, RiftSower.class, MentalJourney.class})
class MountVelusManticoreTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a creature card deals one damage to a target")
    void discardOneTypeCardDealsOneDamage() {
        harness.addToBattlefield(player1, new MountVelusManticore());
        harness.setHand(player1, List.of(new RiftSower()));

        triggerAtBeginningOfCombat(player1);
        resolveMayAndDiscard(0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Rift Sower");
    }

    @Test
    @DisplayName("Discarding an enchantment creature card deals two damage to a target")
    void discardTwoTypeCardDealsTwoDamage() {
        harness.addToBattlefield(player1, new MountVelusManticore());
        harness.setHand(player1, List.of(new MountVelusManticore()));

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
        RiftSower card = new RiftSower();
        harness.setHand(player1, List.of(card));

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
        harness.setHand(player1, List.of(new RiftSower()));

        triggerAtBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty hand cannot create the damage trigger")
    void emptyHandDoesNotDealDamage() {
        harness.addToBattlefield(player1, new MountVelusManticore());
        harness.setHand(player1, List.of());

        triggerAtBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The reflexive trigger can damage a creature")
    void canTargetCreature() {
        harness.addToBattlefield(player1, new MountVelusManticore());
        harness.addToBattlefield(player2, new RiftSower());
        harness.setHand(player1, List.of(new MountVelusManticore()));
        var target = gd.playerBattlefields.get(player2.getId()).getFirst();

        triggerAtBeginningOfCombat(player1);
        resolveMayAndDiscard(0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Rift Sower");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A later discard does not change the damage from the original discarded card")
    void damageUsesOriginalDiscardDespiteCyclingInResponse() {
        harness.addToBattlefield(player1, new MountVelusManticore());
        harness.setHand(player1, List.of(new MountVelusManticore(), new MentalJourney()));
        harness.setLibrary(player1, List.of());

        triggerAtBeginningOfCombat(player1);
        resolveMayAndDiscard(0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mount Velus Manticore");
        harness.assertInGraveyard(player1, "Mental Journey");
        harness.assertLife(player2, 18);
    }

    private void triggerAtBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private void resolveMayAndDiscard(int handIndex) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, handIndex);
    }
}
