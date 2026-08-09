package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YavimayaElderTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the death trigger searches for up to two basic lands")
    void deathTriggerSearchesForBasicLands() {
        harness.addToBattlefield(player1, new YavimayaElder());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Forest forest = new Forest();
        Plains plains = new Plains();
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(forest, plains, bears);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, plains);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).contains(forest, plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
    }

    @Test
    @DisplayName("Declining the death trigger still allows the sacrifice ability to draw")
    void decliningDeathTriggerStillDraws() {
        harness.addToBattlefield(player1, new YavimayaElder());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setLibrary(new GrizzlyBears());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof YavimayaElder);
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
