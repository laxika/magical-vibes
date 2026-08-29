package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EfreetFlamepainterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage targets only instant and sorcery cards from your graveyard")
    void combatDamageTargetsOnlyOwnInstantsAndSorceries() {
        Card ownInstant = new Shock();
        Card ownSorcery = new CounselOfTheSoratami();
        Card ownCreature = new GrizzlyBears();
        Card opponentInstant = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ownInstant, ownSorcery, ownCreature)));
        harness.setGraveyard(player2, List.of(opponentInstant));

        attackDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownInstant.getId(), ownSorcery.getId());
    }

    @Test
    @DisplayName("Combat damage lets you cast the chosen sorcery for free and exiles it")
    void castsChosenSorceryForFreeAndExilesIt() {
        Card counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(counsel)));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        attackDealingDamage();

        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(counsel.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(counsel.getId()));
    }

    @Test
    @DisplayName("Combat damage with no legal card does not prompt")
    void noLegalCardDoesNotPrompt() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        attackDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    private void attackDealingDamage() {
        Permanent efreet = addCreatureReady(player1, new EfreetFlamepainter());
        efreet.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
