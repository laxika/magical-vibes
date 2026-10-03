package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClaimJumper.class, Forest.class, Plains.class})
class ClaimJumperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB searches for a tapped Plains when an opponent controls more lands")
    void searchesForOnePlainsWhenOpponentHasOneMoreLand() {
        castClaimJumper();
        addOpponentLands(1);
        setupLibrary(2);

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        chooseLibraryCard();

        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Plains"))
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB can repeat the search when the opponent still controls more lands")
    void repeatsSearchWhileOpponentStillHasMoreLands() {
        castClaimJumper();
        addOpponentLands(2);
        setupLibrary(3);

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        chooseLibraryCard();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        chooseLibraryCard();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Plains"))
                .hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the first search still allows the repeated search")
    void decliningFirstSearchAllowsRepeat() {
        castClaimJumper();
        addOpponentLands(2);
        setupLibrary(2);

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        chooseLibraryCard();

        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The trigger does not happen when the opponent does not control more lands")
    void doesNotTriggerWhenOpponentDoesNotHaveMoreLands() {
        castClaimJumper();
        addOpponentLands(1);
        addControllerLands(1);
        setupLibrary(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The trigger does nothing if land counts equalize before resolution")
    void doesNothingIfLandCountsEqualizeBeforeResolution() {
        castClaimJumper();
        addOpponentLands(1);
        setupLibrary(1);

        harness.passBothPriorities();
        addControllerLands(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("The process repeats only once even when the opponent still has more lands")
    void searchesForAtMostTwoPlains() {
        castClaimJumper();
        addOpponentLands(4);
        setupLibrary(3);

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        chooseLibraryCard();
        harness.handleMayAbilityChosen(player1, true);
        chooseLibraryCard();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Plains"))
                .hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second search can be declined after finding the first Plains")
    void canDeclineSecondSearch() {
        castClaimJumper();
        addOpponentLands(2);
        setupLibrary(2);

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        chooseLibraryCard();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Plains"))
                .hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining both searches leaves the library untouched")
    void canDeclineBothSearches() {
        castClaimJumper();
        addOpponentLands(2);
        setupLibrary(2);
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(originalLibrary);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Failing to find on the first search still permits the repeated search")
    void failingToFindStillAllowsSecondSearch() {
        castClaimJumper();
        addOpponentLands(1);
        setupLibrary(1);

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        chooseLibraryCard();

        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching a library without Plains finishes both searches without finding a card")
    void noMatchingPlainsDoesNotPreventRepeatOrFinish() {
        castClaimJumper();
        addOpponentLands(1);
        setupLibrary(0);

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castClaimJumper() {
        harness.setHand(player1, List.of(new ClaimJumper()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveCreatureAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void chooseLibraryCard() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
    }

    private void addOpponentLands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
    }

    private void addControllerLands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
    }

    private void setupLibrary(int plainsCount) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < plainsCount; i++) {
            deck.add(new Plains());
        }
        deck.add(new ClaimJumper());
        harness.setLibrary(player1, deck);
    }
}
