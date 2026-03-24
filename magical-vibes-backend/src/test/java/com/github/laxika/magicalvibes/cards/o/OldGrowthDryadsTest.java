package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OldGrowthDryadsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB triggers opponent search for basic land to battlefield tapped")
    void etbTriggersOpponentSearch() {
        harness.setHand(player1, List.of(new OldGrowthDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        setupLibrary(player2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell, ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        // Opponent (player2) is prompted to search
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.librarySearch().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Controller is not prompted to search")
    void controllerIsNotPromptedToSearch() {
        harness.setHand(player1, List.of(new OldGrowthDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        setupLibrary(player1);
        setupLibrary(player2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        // Opponent gets the search, not the controller
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Opponent can choose a basic land and it enters tapped")
    void opponentLandEntersTapped() {
        harness.setHand(player1, List.of(new OldGrowthDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        setupLibrary(player2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player2.getId()).size();

        // Opponent picks a basic land
        harness.getGameService().handleLibraryCardChosen(gd, player2, 0);

        // Land entered the battlefield tapped
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND)
                        && p.getCard().getSupertypes().contains(CardSupertype.BASIC)
                        && p.isTapped());
    }

    @Test
    @DisplayName("Opponent can fail to find (decline search)")
    void opponentCanFailToFind() {
        harness.setHand(player1, List.of(new OldGrowthDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        setupLibrary(player2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player2.getId()).size();

        // Opponent declines (fail to find, index -1)
        harness.getGameService().handleLibraryCardChosen(gd, player2, -1);

        // No land entered the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldBefore);
        // Search is done
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
    }

    @Test
    @DisplayName("No search prompt when opponent has no basic lands in library")
    void noBasicLandsInOpponentLibrary() {
        harness.setHand(player1, List.of(new OldGrowthDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Opponent has no basic lands in library
        List<Card> deck2 = harness.getGameData().playerDecks.get(player2.getId());
        deck2.clear();
        deck2.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger (no basic lands found)

        GameData gd = harness.getGameData();
        // No search prompt (opponent had no basic lands)
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
    }

    @Test
    @DisplayName("Old-Growth Dryads enters the battlefield as a 3/3")
    void entersAsThreeThree() {
        harness.setHand(player1, List.of(new OldGrowthDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Clear opponent's library so search is skipped
        harness.getGameData().playerDecks.get(player2.getId()).clear();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger (empty library)

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Old-Growth Dryads")
                        && p.getCard().getPower() == 3
                        && p.getCard().getToughness() == 3);
    }

    private void setupLibrary(Player player) {
        List<Card> deck = harness.getGameData().playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Mountain(), new GrizzlyBears()));
    }
}
