package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearOfSurveillance.class, GrizzlyBears.class})
class FearOfSurveillanceTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking surveils 1")
    void attackingSurveilsOne() {
        addCreatureReady(player1, new FearOfSurveillance());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.MayAbilityChoice surveil =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(surveil).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the attack surveil leaves the top card on the library")
    void decliningAttackSurveilLeavesTopCardOnLibrary() {
        addCreatureReady(player1, new FearOfSurveillance());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Attacking puts only the top card into the graveyard")
    void attackingSurveilsOnlyTopCard() {
        addCreatureReady(player1, new FearOfSurveillance());
        Card topCard = new FearOfSurveillance();
        Card secondCard = new FearOfSurveillance();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("Attacking with an empty library finishes without a choice")
    void attackingWithEmptyLibrary() {
        addCreatureReady(player1, new FearOfSurveillance());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The attacking controller surveils their own library")
    void attackingControllerSurveilsOwnLibrary() {
        addCreatureReady(player2, new FearOfSurveillance());
        Card defendingTopCard = new FearOfSurveillance();
        Card attackingTopCard = new FearOfSurveillance();
        harness.setLibrary(player1, List.of(defendingTopCard));
        harness.setLibrary(player2, List.of(attackingTopCard));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(attackingTopCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(defendingTopCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
