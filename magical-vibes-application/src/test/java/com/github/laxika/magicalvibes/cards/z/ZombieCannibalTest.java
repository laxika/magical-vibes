package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ZombieCannibalTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may exile one chosen card from the damaged player's graveyard")
    void combatDamageExilesOneChosenCard() {
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        harness.setGraveyard(player2, List.of(bears, giant));

        attackDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();

        assertThat(graveyardNames(player2.getId())).containsExactly("Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Declining the optional graveyard exile exiles nothing")
    void decliningExileExilesNothing() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        attackDealingDamage();

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(graveyardNames(player2.getId())).containsExactly("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the damaged player's graveyard is offered")
    void controllerGraveyardIsNotOffered() {
        Card ownBears = new GrizzlyBears();
        Card theirGiant = new HillGiant();
        harness.setGraveyard(player1, List.of(ownBears));
        harness.setGraveyard(player2, List.of(theirGiant));

        attackDealingDamage();

        List<UUID> valid = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(valid).containsExactly(theirGiant.getId());
    }

    @Test
    @DisplayName("An empty graveyard presents no choice")
    void emptyGraveyardPresentsNoChoice() {
        harness.setGraveyard(player2, List.of());

        attackDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private List<String> graveyardNames(UUID playerId) {
        return gd.playerGraveyards.get(playerId).stream().map(Card::getName).toList();
    }

    private void attackDealingDamage() {
        Permanent zombieCannibal = addCreatureReady(player1, new ZombieCannibal());
        zombieCannibal.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
