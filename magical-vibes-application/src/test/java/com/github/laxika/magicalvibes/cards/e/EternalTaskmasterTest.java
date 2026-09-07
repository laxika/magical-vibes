package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({EternalTaskmaster.class, GrizzlyBears.class, Shock.class})
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
        Card eligible = new GrizzlyBears();
        Card nonCreature = new Shock();
        Card opponentCreature = new GrizzlyBears();
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
        Card creature = new GrizzlyBears();
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
        Card creature = new GrizzlyBears();
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

    private void addTriggerMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
