package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoulderbornDragon.class, Forest.class})
class BoulderbornDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking surveils 1")
    void attackingSurveilsOne() {
        addCreatureReady(player1, new BoulderbornDragon());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        PendingInteraction.MayAbilityChoice surveil =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(surveil).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the attack surveil leaves the top card on the library")
    void decliningAttackSurveilLeavesTopCardOnLibrary() {
        addCreatureReady(player1, new BoulderbornDragon());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Attack surveil moves only the top card to the graveyard")
    void attackSurveilsOnlyOneCard() {
        addCreatureReady(player1, new BoulderbornDragon());
        Card topCard = new Forest();
        Card nextCard = new BoulderbornDragon();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("Attack surveil with an empty library completes without a choice")
    void attackSurveilWithEmptyLibrary() {
        addCreatureReady(player1, new BoulderbornDragon());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The attacking Dragon's controller surveils their own library")
    void opposingControllerSurveilsTheirOwnLibrary() {
        addCreatureReady(player2, new BoulderbornDragon());
        Card ownTopCard = new Forest();
        Card otherTopCard = new BoulderbornDragon();
        harness.setLibrary(player2, List.of(ownTopCard));
        harness.setLibrary(player1, List.of(otherTopCard));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(ownTopCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherTopCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
