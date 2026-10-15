package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WelkinHawk.class, RagingGoblin.class})
class WelkinHawkTest extends BaseCardTest {

    @Test
    @DisplayName("When Welkin Hawk dies, accepting the trigger searches for a Welkin Hawk")
    void deathTriggerSearchesForWelkinHawk() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new WelkinHawk());
        Card hawkInLibrary = new WelkinHawk();
        harness.setLibrary(player1, List.of(hawkInLibrary));

        destroyHawk(hawk);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(hawkInLibrary);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(hawkInLibrary);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(hawk.getCard().getId()));
    }

    @Test
    @DisplayName("Declining Welkin Hawk's death trigger leaves it in the graveyard")
    void decliningDeathTriggerLeavesHawkInGraveyard() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new WelkinHawk());
        harness.setLibrary(player1, List.of(new WelkinHawk()));

        destroyHawk(hawk);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(hawk.getCard().getId()));
    }

    @Test
    @DisplayName("When no Welkin Hawk is in the library, accepting the trigger finds nothing")
    void acceptingDeathTriggerWithNoMatchingCardFindsNothing() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new WelkinHawk());
        harness.setLibrary(player1, List.of());

        destroyHawk(hawk);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(hawk.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(hawk.getCard().getId()));
    }

    @Test
    @DisplayName("The death trigger puts only one matching Hawk into hand")
    void searchesForOnlyOneHawk() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new WelkinHawk());
        Card first = new WelkinHawk();
        Card second = new WelkinHawk();
        Card opponentsHawk = new WelkinHawk();
        Card nonmatchingCard = new RagingGoblin();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(nonmatchingCard, first, second));
        harness.setLibrary(player2, List.of(opponentsHawk));

        destroyHawk(hawk);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(first, second);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonmatchingCard, first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsHawk);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An accepted search may find nothing even when a Hawk is available")
    void mayFailToFindMatchingHawk() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new WelkinHawk());
        Card hawkInLibrary = new WelkinHawk();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(hawkInLibrary));

        destroyHawk(hawk);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hawkInLibrary);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void destroyHawk(Permanent hawk) {
        hawk.setMarkedDamage(1);
        harness.runStateBasedActions();
    }
}
