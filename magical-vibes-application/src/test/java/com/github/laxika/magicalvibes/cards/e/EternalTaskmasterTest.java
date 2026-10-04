package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ChainwhipCyclops;
import com.github.laxika.magicalvibes.cards.s.SorinsThirst;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EternalTaskmaster.class, ChainwhipCyclops.class, SorinsThirst.class})
class EternalTaskmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new EternalTaskmaster()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent taskmaster = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(taskmaster.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attack trigger targets only a creature card in your graveyard")
    void targetsOnlyOwnCreatureCards() {
        addReadyTaskmaster();
        Card eligible = new ChainwhipCyclops();
        Card nonCreature = new SorinsThirst();
        Card opponentCreature = new ChainwhipCyclops();
        harness.setGraveyard(player1, List.of(eligible, nonCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));

        declareAttackers(player1, List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
    }

    @Test
    @DisplayName("Paying {2}{B} returns the targeted creature card to hand")
    void payingReturnsTargetedCreature() {
        addReadyTaskmaster();
        Card creature = new ChainwhipCyclops();
        harness.setGraveyard(player1, List.of(creature));
        addTriggerMana();

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Declining the payment leaves the targeted creature card in the graveyard")
    void decliningPaymentDoesNotReturnCreature() {
        addReadyTaskmaster();
        Card creature = new ChainwhipCyclops();
        harness.setGraveyard(player1, List.of(creature));
        addTriggerMana();

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    private void addReadyTaskmaster() {
        addCreatureReady(player1, new EternalTaskmaster());
    }

    @Test
    @DisplayName("An attack without a legal graveyard target offers no payment")
    void noLegalTargetOffersNoPayment() {
        addReadyTaskmaster();
        harness.setGraveyard(player1, List.of(new SorinsThirst()));
        harness.setGraveyard(player2, List.of(new ChainwhipCyclops()));
        addTriggerMana();

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("An illegal target at resolution prevents the payment choice")
    void removedTargetPreventsPayment() {
        addReadyTaskmaster();
        Card creature = new ChainwhipCyclops();
        harness.setGraveyard(player1, List.of(creature));
        addTriggerMana();

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger resolves independently of its source and returns only its target")
    void sourceLeavingDoesNotStopReturn() {
        addReadyTaskmaster();
        Card target = new ChainwhipCyclops();
        Card other = new ChainwhipCyclops();
        harness.setGraveyard(player1, List.of(target, other));
        addTriggerMana();

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId).contains(target.getId()).doesNotContain(other.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).containsExactly(other.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void addTriggerMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
