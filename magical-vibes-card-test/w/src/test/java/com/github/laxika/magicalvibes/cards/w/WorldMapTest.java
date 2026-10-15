package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StartingTown;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WorldMap.class, StartingTown.class, Forest.class, Plains.class})
class WorldMapTest extends BaseCardTest {

    @Test
    @DisplayName("The one-mana ability sacrifices World Map and searches for a basic land")
    void searchesForBasicLand() {
        addMapAndMana(1);
        setLibrary(new Plains(), new Forest(), new StartingTown(), new WorldMap());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(2)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        harness.assertInGraveyard(player1, "World Map");
    }

    @Test
    @DisplayName("The three-mana ability searches for any land")
    void searchesForAnyLand() {
        addMapAndMana(3);
        setLibrary(new Plains(), new StartingTown(), new WorldMap());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Starting Town");
        harness.assertInGraveyard(player1, "World Map");
    }

    @Test
    @DisplayName("Choosing a searched land puts it into hand")
    void chosenLandEntersHand() {
        addMapAndMana(1);
        setLibrary(new Plains(), new WorldMap());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "1, 3"})
    @DisplayName("Both abilities pay mana and sacrifice World Map before resolving")
    void paysCostsBeforeResolution(int abilityIndex, int manaCost) {
        addMapAndMana(manaCost + 1);
        setLibrary(new Plains());

        harness.activateAbility(player1, 0, abilityIndex, null, null);

        harness.assertInGraveyard(player1, "World Map");
        harness.assertNotOnBattlefield(player1, "World Map");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "1, 3"})
    @DisplayName("Neither ability can be activated while World Map is tapped")
    void cannotActivateWhileTapped(int abilityIndex, int manaCost) {
        addMapAndMana(manaCost);
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "World Map");
        harness.assertNotInGraveyard(player1, "World Map");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(manaCost);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "1, 3"})
    @DisplayName("Neither ability can be activated without enough mana")
    void cannotActivateWithoutEnoughMana(int abilityIndex, int manaCost) {
        addMapAndMana(manaCost - 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "World Map");
        harness.assertNotInGraveyard(player1, "World Map");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(manaCost - 1);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "1, 3"})
    @DisplayName("Both abilities may fail to find an available land and still shuffle")
    void mayFailToFindAvailableLand(int abilityIndex, int manaCost) {
        addMapAndMana(manaCost);
        Plains land = new Plains();
        setLibrary(land);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "1, 3"})
    @DisplayName("Both abilities finish and shuffle when no land can be found")
    void resolvesWithoutMatchingLand(int abilityIndex, int manaCost) {
        addMapAndMana(manaCost);
        WorldMap nonland = new WorldMap();
        setLibrary(nonland);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        harness.assertInGraveyard(player1, "World Map");
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "1, 3"})
    @DisplayName("Both abilities finish normally with an empty library")
    void resolvesWithEmptyLibrary(int abilityIndex, int manaCost) {
        addMapAndMana(manaCost);
        setLibrary();
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "World Map");
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "1, 3"})
    @DisplayName("Both abilities reveal the chosen land, put it into hand, and shuffle")
    void revealsLandAndShuffles(int abilityIndex, int manaCost) {
        addMapAndMana(manaCost);
        Card chosenLand = abilityIndex == 0 ? new Plains() : new StartingTown();
        Forest remainingLand = new Forest();
        setLibrary(chosenLand, remainingLand);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, abilityIndex, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1).contains(chosenLand);
        harness.assertNotOnBattlefield(player1, chosenLand.getName());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingLand);
        assertThat(gameLogContains("reveals " + chosenLand.getName())).isTrue();
        assertThat(gameLogContains("Library is shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void addMapAndMana(int amount) {
        harness.addToBattlefield(player1, new WorldMap());
        harness.addMana(player1, ManaColor.COLORLESS, amount);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
