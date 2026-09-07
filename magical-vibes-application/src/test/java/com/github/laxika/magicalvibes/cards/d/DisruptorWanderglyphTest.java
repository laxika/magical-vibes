package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DisruptorWanderglyph.class, GrizzlyBears.class})
class DisruptorWanderglyphTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking exiles a targeted card from the defending player's graveyard")
    void attackExilesDefendingPlayerGraveyardCard() {
        addReadyAttacker();
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));

        declareAttack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(graveyardCard.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCard.getId()));
    }

    @Test
    @DisplayName("A card in the attacker's own graveyard is not a legal target")
    void ownGraveyardCardNotTargetable() {
        addReadyAttacker();
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ownCard)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentCard)));

        declareAttack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(opponentCard.getId());
        assertThat(choice.validCardIds()).doesNotContain(ownCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(ownCard.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(opponentCard.getId()));
    }

    @Test
    @DisplayName("An empty defending graveyard produces no target choice")
    void emptyDefendingGraveyardNoChoice() {
        addReadyAttacker();
        Card ownCard = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ownCard)));

        declareAttack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
    }

    private Permanent addReadyAttacker() {
        Permanent wanderglyph = new Permanent(new DisruptorWanderglyph());
        wanderglyph.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(wanderglyph);
        return wanderglyph;
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }
}
