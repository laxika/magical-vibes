package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.OranRiefTheVastwood;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExpeditionMap.class, Forest.class, Island.class, GrizzlyBears.class, OranRiefTheVastwood.class})
class ExpeditionMapTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Expedition Map sacrifices it")
    void activatingSacrificesSelf() {
        addMapAndMana();

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Expedition Map");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving presents all land cards for the search")
    void resolvingPresentsLandCards() {
        addMapAndMana();
        setLibrary(new Forest(), new Island(), new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(2)
                .allMatch(card -> card.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Choosing a land puts it into hand")
    void chosenLandEntersHand() {
        addMapAndMana();
        setLibrary(new Forest(), new Island(), new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may fail to find a land")
    void searchMayFailToFind() {
        addMapAndMana();
        setLibrary(new GrizzlyBears(), new GrizzlyBears());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Cannot activate Expedition Map without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new ExpeditionMap());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A nonbasic land is revealed and put into hand, then the library is shuffled")
    void findsNonbasicLand() {
        addMapAndMana();
        OranRiefTheVastwood land = new OranRiefTheVastwood();
        Forest otherLand = new Forest();
        setLibrary(land, otherLand);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Oran-Rief, the Vastwood");
        harness.assertNotOnBattlefield(player1, "Oran-Rief, the Vastwood");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherLand);
        assertThat(gd.gameLog).anyMatch(entry ->
                entry.plainText().contains("reveals Oran-Rief, the Vastwood"));
        assertThat(gd.gameLog).anyMatch(entry ->
                entry.plainText().contains("Library is shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May fail to find even when the library contains a land")
    void mayDeclineAvailableLand() {
        addMapAndMana();
        Forest land = new Forest();
        setLibrary(land);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.gameLog).anyMatch(entry ->
                entry.plainText().contains("Library is shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate a tapped Expedition Map")
    void cannotActivateWhenTapped() {
        addMapAndMana();
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Expedition Map");
        harness.assertNotInGraveyard(player1, "Expedition Map");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching an empty library completes without drawing a card")
    void emptyLibrarySearchCompletes() {
        addMapAndMana();
        setLibrary();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void addMapAndMana() {
        harness.addToBattlefield(player1, new ExpeditionMap());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
