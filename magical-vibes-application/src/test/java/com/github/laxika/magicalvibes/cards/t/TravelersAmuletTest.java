package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TravelersAmulet.class, Plains.class, Forest.class, Island.class, WalkingCorpse.class})
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
        harness.assertNotOnBattlefield(player1, "Traveler's Amulet");
        harness.assertInGraveyard(player1, "Traveler's Amulet");

        // Should be awaiting library search with only basic lands offered
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
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

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals(chosenName));
    }

    @Test
    @DisplayName("Empty library auto-completes without searching")
    void emptyLibraryAutoCompletes() {
        harness.addToBattlefield(player1, new TravelersAmulet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Traveler's Amulet");
        harness.assertInGraveyard(player1, "Traveler's Amulet");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not require tap to activate")
    void doesNotRequireTap() {
        harness.addToBattlefield(player1, new TravelersAmulet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerDecks.get(player1.getId()).clear();

        // Tap the amulet first — ability should still be activatable since it doesn't require tap
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        harness.activateAbility(player1, 0, null, null);

        // Amulet should be sacrificed even though it was tapped
        harness.assertNotOnBattlefield(player1, "Traveler's Amulet");
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution, then the found land is revealed and put into hand")
    void sacrificePrecedesSearchAndFoundLandIsRevealed() {
        harness.addToBattlefield(player1, new TravelersAmulet());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of());
        Plains land = new Plains();
        harness.setLibrary(player1, List.of(land, new Forest()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Traveler's Amulet");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(land);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals Plains"));
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled."));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A basic land search may fail to find even when a basic land is available")
    void mayDeclineAvailableBasicLand() {
        harness.addToBattlefield(player1, new TravelersAmulet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of());
        Plains land = new Plains();
        harness.setLibrary(player1, List.of(land));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.assertInGraveyard(player1, "Traveler's Amulet");
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled."));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A library with no basic lands completes the search and shuffles")
    void noBasicLandsCompletesSearch() {
        harness.addToBattlefield(player1, new TravelersAmulet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of());
        WalkingCorpse creature = new WalkingCorpse();
        harness.setLibrary(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled."));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void setupLibraryWithBasicLands() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new WalkingCorpse()));
    }
}
