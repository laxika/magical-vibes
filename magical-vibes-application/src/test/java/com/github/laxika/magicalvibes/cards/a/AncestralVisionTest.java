package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.v.VedalkenOrrery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncestralVision.class, VedalkenOrrery.class})
class AncestralVisionTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Ancestral Vision with four time counters")
    void suspendExilesWithFourTimeCounters() {
        AncestralVision card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Suspend counters are removed only during Ancestral Vision's owner's upkeep")
    void suspendCountersRemainThroughOpponentsUpkeep() {
        AncestralVision card = suspendCard();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast that draws three cards for the target player")
    void lastCounterOffersFreeCastAndDrawsForTargetPlayer() {
        AncestralVision card = suspendCard();
        int targetHandBefore = gd.playerHands.get(player2.getId()).size();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(targetHandBefore + 3);
        harness.assertInGraveyard(player1, "Ancestral Vision");
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Ancestral Vision in exile")
    void decliningSuspendCastLeavesCardInExile() {
        AncestralVision card = suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotInGraveyard(player1, "Ancestral Vision");
    }

    @Test
    @DisplayName("Removing the last counter puts a separate cast trigger on the stack")
    void lastCounterCreatesRespondableCastTrigger() {
        AncestralVision card = suspendCard();
        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Ancestral Vision can target its own controller and draws only on resolution")
    void suspendCastCanTargetController() {
        suspendCard();
        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore + 3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore);
        harness.assertInGraveyard(player1, "Ancestral Vision");
    }

    @Test
    @DisplayName("Ancestral Vision cannot be cast normally from hand without an alternative cost")
    void cannotCastNormallyFromHand() {
        AncestralVision card = new AncestralVision();
        harness.setHand(player1, List.of(card));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Suspend requires blue mana and keeps the card in hand if payment fails")
    void suspendRequiresBlueMana() {
        AncestralVision card = new AncestralVision();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ancestral Vision cannot be suspended during upkeep without flash permission")
    void cannotSuspendDuringUpkeep() {
        AncestralVision card = new AncestralVision();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @CardUsed({AncestralVision.class, VedalkenOrrery.class})
    @DisplayName("Flash permission allows suspending Ancestral Vision during an opponent's turn")
    void canSuspendWithFlashPermissionDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        AncestralVision card = new AncestralVision();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    private AncestralVision suspendCard() {
        AncestralVision card = new AncestralVision();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
