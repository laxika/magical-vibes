package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TravelersAmuletTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices amulet and searches library for basic land")
    void activateAbilitySacrificesAndSearches() {
        harness.addToBattlefield(player1, new TravelersAmulet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibraryWithBasicLands();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Amulet should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Traveler's Amulet"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Traveler's Amulet"));

        // Should be awaiting library search with only basic lands offered
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().cards()).hasSize(3);
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
    }

    @Test
    @DisplayName("Choosing a basic land from search puts it into hand")
    void choosingBasicLandPutsItIntoHand() {
        harness.addToBattlefield(player1, new TravelersAmulet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibraryWithBasicLands();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.librarySearch().cards();
        String chosenName = offered.getFirst().getName();

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
    }

    @Test
    @DisplayName("Empty library auto-completes without searching")
    void emptyLibraryAutoCompletes() {
        harness.addToBattlefield(player1, new TravelersAmulet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);

        // Amulet should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Traveler's Amulet"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Traveler's Amulet"));
    }

    @Test
    @DisplayName("Does not require tap to activate")
    void doesNotRequireTap() {
        harness.addToBattlefield(player1, new TravelersAmulet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerDecks.get(player1.getId()).clear();

        // Tap the amulet first — ability should still be activatable since it doesn't require tap
        gd.playerBattlefields.get(player1.getId()).getFirst().setTapped(true);

        harness.activateAbility(player1, 0, null, null);

        // Amulet should be sacrificed even though it was tapped
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Traveler's Amulet"));
    }

    private void setupLibraryWithBasicLands() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
