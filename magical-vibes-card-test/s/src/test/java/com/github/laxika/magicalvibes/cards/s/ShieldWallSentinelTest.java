package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WalkingBulwark;
import com.github.laxika.magicalvibes.cards.y.YavimayaSojourner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShieldWallSentinel.class, WalkingBulwark.class, YavimayaSojourner.class})
class ShieldWallSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB search offers only creature cards with defender")
    void searchOffersOnlyCreaturesWithDefender() {
        castSentinel();
        harness.setLibrary(player1, List.of(new WalkingBulwark(), new YavimayaSojourner()));

        resolveSentinelAndAcceptSearch();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName).containsExactly("Walking Bulwark");
    }

    @Test
    @DisplayName("Choosing a creature with defender puts it into hand")
    void chosenCreatureWithDefenderGoesToHand() {
        castSentinel();
        harness.setLibrary(player1, List.of(new YavimayaSojourner(), new WalkingBulwark()));

        resolveSentinelAndAcceptSearch();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Walking Bulwark");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the search does not move a card")
    void decliningSearchDoesNothing() {
        castSentinel();
        harness.setLibrary(player1, List.of(new WalkingBulwark()));

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An accepted search may find nothing even when a defender is available")
    void mayFailToFindAvailableDefender() {
        castSentinel();
        Card defender = new WalkingBulwark();
        harness.setLibrary(player1, List.of(defender));

        resolveSentinelAndAcceptSearch();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(defender);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting with no matching cards still shuffles")
    void noMatchingCardsStillShuffles() {
        castSentinel();
        Card nondefender = new YavimayaSojourner();
        harness.setLibrary(player1, List.of(nondefender));

        resolveSentinelAndAcceptSearch();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nondefender);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting with an empty library completes the search")
    void emptyLibraryCompletesSearch() {
        castSentinel();
        harness.setLibrary(player1, List.of());

        resolveSentinelAndAcceptSearch();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The selected defender is revealed and only that card is taken before shuffling")
    void selectedDefenderIsRevealedAndRemainingCardsStayInLibrary() {
        castSentinel();
        Card firstDefender = new WalkingBulwark();
        Card selectedDefender = new ShieldWallSentinel();
        Card nondefender = new YavimayaSojourner();
        harness.setLibrary(player1, List.of(nondefender, firstDefender, selectedDefender));

        resolveSentinelAndAcceptSearch();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selectedDefender);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nondefender, firstDefender);
        assertThat(gameLogContains("reveals Shield-Wall Sentinel")).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castSentinel() {
        harness.setHand(player1, List.of(new ShieldWallSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private void resolveSentinelAndAcceptSearch() {
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
    }
}
