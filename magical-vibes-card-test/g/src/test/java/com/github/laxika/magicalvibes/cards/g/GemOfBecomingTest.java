package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GemOfBecoming.class, Island.class, Swamp.class, Mountain.class, Plains.class})
class GemOfBecomingTest extends BaseCardTest {

    @Test
    void searchesForOneOfEachSubtypeRevealsAndPutsThemIntoHand() {
        startSearch(List.of(new Island(), new Island(), new Swamp(), new Mountain(), new Plains()));
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Gem of Becoming");
        harness.assertInGraveyard(player1, "Gem of Becoming");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();
        assertChoices("Island", "Island");
        harness.handleCardChosen(player1, 0);
        assertChoices("Swamp");
        harness.handleCardChosen(player1, 0);
        assertChoices("Mountain");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        harness.assertInHand(player1, "Island");
        harness.assertInHand(player1, "Swamp");
        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Island", "Plains");
        assertThat(gameLogContains("reveals Island")).isTrue();
        assertThat(gameLogContains("reveals Swamp")).isTrue();
        assertThat(gameLogContains("reveals Mountain")).isTrue();
        assertThat(gameLogContains("library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canFailToFindOneSubtypeAndStillFindTheOthers() {
        startSearch(List.of(new Island(), new Swamp(), new Mountain()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        assertChoices("Swamp");
        harness.handleCardChosen(player1, 0);
        assertChoices("Mountain");
        harness.handleCardChosen(player1, 0);

        harness.assertNotInHand(player1, "Island");
        harness.assertInHand(player1, "Swamp");
        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canFailToFindAllThreeEvenWhenTheyExist() {
        startSearch(List.of(new Island(), new Swamp(), new Mountain()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gameLogContains("library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void skipsMissingSubtypesAndFindsMountain() {
        startSearch(List.of(new Mountain(), new Plains()));
        harness.passBothPriorities();
        assertChoices("Mountain");
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithNoMatchingCards() {
        startSearch(List.of(new Plains()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Plains");
        assertThat(gameLogContains("library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithEmptyLibrary() {
        startSearch(List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new GemOfBecoming());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Gem of Becoming");
        harness.assertNotInGraveyard(player1, "Gem of Becoming");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void insufficientManaDoesNotTapOrSacrificeGem() {
        harness.addToBattlefield(player1, new GemOfBecoming());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Gem of Becoming");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void startSearch(List<? extends Card> library) {
        harness.addToBattlefield(player1, new GemOfBecoming());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, library);
        harness.activateAbility(player1, 0, null, null);
    }

    private void assertChoices(String... names) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName).containsExactlyInAnyOrder(names);
    }
}
