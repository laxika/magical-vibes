package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TheNotaryHobbits;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HobbitHole.class, Forest.class, TheNotaryHobbits.class})
class HobbitHoleTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability searches for a basic land and puts it onto the battlefield tapped")
    void sacrificesAndSearchesForBasicLand() {
        harness.addToBattlefield(player1, new HobbitHole());
        harness.setLibrary(player1, List.of(new Forest(), new HobbitHole(), new TheNotaryHobbits()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertInGraveyard(player1, "Hobbit Hole");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Hobbit Hole");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest") && permanent.isTapped());
    }

    @Test
    @DisplayName("Halflingcycling searches for a Halfling and puts it into hand")
    void halflingcyclingSearchesForHalfling() {
        harness.setHand(player1, List.of(new HobbitHole()));
        harness.setLibrary(player1, List.of(new TheNotaryHobbits(), new Forest(), new HobbitHole()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Hobbit Hole");
        harness.assertNotInHand(player1, "Hobbit Hole");
        harness.assertNotInHand(player1, "The Notary Hobbits");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("The Notary Hobbits");
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Hobbit Hole");
        harness.assertInHand(player1, "The Notary Hobbits");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("reveals The Notary Hobbits"));
    }

    @Test
    void cannotActivateSacrificeAbilityWhileTapped() {
        harness.addToBattlefield(player1, new HobbitHole());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Hobbit Hole");
        harness.assertNotInGraveyard(player1, "Hobbit Hole");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void halflingcyclingRequiresFourManaBeforeDiscarding() {
        harness.setHand(player1, List.of(new HobbitHole()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Hobbit Hole");
        harness.assertNotInGraveyard(player1, "Hobbit Hole");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayDeclineBasicLandSearchEvenWithMatchingCard() {
        harness.addToBattlefield(player1, new HobbitHole());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Hobbit Hole");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("Library is shuffled"));
    }

    @Test
    void mayDeclineHalflingSearchEvenWithMatchingCard() {
        harness.setHand(player1, List.of(new HobbitHole()));
        harness.setLibrary(player1, List.of(new TheNotaryHobbits()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        harness.assertInGraveyard(player1, "Hobbit Hole");
        harness.assertNotInHand(player1, "The Notary Hobbits");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("The Notary Hobbits");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("Library is shuffled"));
    }

    @Test
    void basicLandSearchWithNoMatchingCardsStillResolves() {
        harness.addToBattlefield(player1, new HobbitHole());
        harness.setLibrary(player1, List.of(new HobbitHole()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hobbit Hole");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Hobbit Hole");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("Library is shuffled"));
    }

    @Test
    void halflingcyclingWithEmptyLibraryStillDiscardsAndResolves() {
        harness.setHand(player1, List.of(new HobbitHole()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hobbit Hole");
        harness.assertNotInHand(player1, "Hobbit Hole");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
