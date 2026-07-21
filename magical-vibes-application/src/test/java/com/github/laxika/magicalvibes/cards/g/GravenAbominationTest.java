package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

class GravenAbominationTest extends BaseCardTest {

    private Permanent addReadyAttacker() {
        Permanent abomination = new Permanent(new GravenAbomination());
        abomination.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(abomination);
        return abomination;
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Attacking exiles a targeted card from the defending player's graveyard")
    void attackExilesDefendingPlayerGraveyardCard() {
        addReadyAttacker();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        declareAttack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
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
                .anyMatch(c -> c.getId().equals(ownCard.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(opponentCard.getId()));
    }

    @Test
    @DisplayName("Empty defending graveyard produces no target choice")
    void emptyDefendingGraveyardNoChoice() {
        addReadyAttacker();
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        declareAttack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }
}
