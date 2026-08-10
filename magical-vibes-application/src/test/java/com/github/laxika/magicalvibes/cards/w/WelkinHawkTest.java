package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WelkinHawkTest extends BaseCardTest {

    @Test
    @DisplayName("When Welkin Hawk dies, accepting the trigger searches for a Welkin Hawk")
    void deathTriggerSearchesForWelkinHawk() {
        harness.addToBattlefield(player1, new WelkinHawk());
        Permanent hawk = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card hawkInLibrary = new WelkinHawk();
        setLibrary(hawkInLibrary, new GrizzlyBears());

        destroyBattlefieldWithWrath();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(hawkInLibrary);
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(hawkInLibrary);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(hawk.getCard().getId()));
    }

    @Test
    @DisplayName("Declining Welkin Hawk's death trigger leaves it in the graveyard")
    void decliningDeathTriggerLeavesHawkInGraveyard() {
        harness.addToBattlefield(player1, new WelkinHawk());
        Permanent hawk = gd.playerBattlefields.get(player1.getId()).getFirst();
        setLibrary(new WelkinHawk());

        destroyBattlefieldWithWrath();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(hawk.getCard().getId()));
    }

    private void destroyBattlefieldWithWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
