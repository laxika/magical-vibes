package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IlMhegPixie.class})
class IlMhegPixieTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking surveils 1")
    void attackingSurveilsOne() {
        addCreatureReady(player1, new IlMhegPixie());
        Card topCard = new IlMhegPixie();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.MayAbilityChoice surveil =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(surveil).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Il Mheg Pixie");
    }

    @Test
    @DisplayName("Declining the attack surveil leaves the top card on the library")
    void decliningAttackSurveilLeavesTopCardOnLibrary() {
        addCreatureReady(player1, new IlMhegPixie());
        Card topCard = new IlMhegPixie();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertNotInGraveyard(player1, "Il Mheg Pixie");
    }

    @Test
    @DisplayName("Attack surveil puts only the top card into the graveyard")
    void attackSurveilsOnlyTheTopCard() {
        addCreatureReady(player1, new IlMhegPixie());
        Card topCard = new IlMhegPixie();
        Card secondCard = new IlMhegPixie();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Attack surveil with an empty library does not ask for a choice")
    void attackSurveilWithEmptyLibrary() {
        addCreatureReady(player1, new IlMhegPixie());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The attacking Pixie's controller surveils their own library")
    void otherPlayerSurveilsTheirOwnLibrary() {
        addCreatureReady(player2, new IlMhegPixie());
        Card controllerTopCard = new IlMhegPixie();
        Card opponentTopCard = new IlMhegPixie();
        harness.setLibrary(player2, List.of(controllerTopCard));
        harness.setLibrary(player1, List.of(opponentTopCard));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(controllerTopCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentTopCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
