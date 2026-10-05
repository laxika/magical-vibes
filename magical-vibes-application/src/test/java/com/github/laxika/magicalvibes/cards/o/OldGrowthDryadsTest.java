package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RavenousDaggertooth;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OldGrowthDryads.class, Plains.class, Forest.class, Mountain.class, RavenousDaggertooth.class})
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
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
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
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2.getId());
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
        harness.handleCardChosen(player2, 0);

        // Land entered the battlefield tapped
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND)
                        && p.getCard().getSupertypes().contains(CardSupertype.BASIC)
                        && p.isTapped());
    }

    @Test
    @DisplayName("Opponent can search and fail to find")
    void opponentCanFailToFind() {
        harness.setHand(player1, List.of(new OldGrowthDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        setupLibrary(player2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player2.getId()).size();

        // Opponent fails to find a basic land
        harness.handleCardChosen(player2, -1);

        // No land entered the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldBefore);
        // Search is done
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Opponent with no basic lands can decline without searching or shuffling")
    void opponentWithoutBasicLandsCanDecline() {
        harness.setHand(player1, List.of(new OldGrowthDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Opponent has no basic lands in library
        harness.setLibrary(player2, List.of(new RavenousDaggertooth(), new RavenousDaggertooth()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger (no basic lands found)

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, false);
        } else if (gd.interaction.activeInteraction() instanceof PendingInteraction.LibrarySearch) {
            harness.handleCardChosen(player2, -1);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains(player2.getUsername() + " searches")
                || entry.plainText().contains("Library is shuffled"));
    }

    @Test
    @DisplayName("Old-Growth Dryads enters successfully with an empty opposing library")
    void entersWithEmptyOpponentLibrary() {
        harness.setHand(player1, List.of(new OldGrowthDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Clear opponent's library so search is skipped
        harness.setLibrary(player2, List.of());

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger (empty library)

        harness.assertOnBattlefield(player1, "Old-Growth Dryads");
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent can decline the optional search without searching or shuffling")
    void opponentCanDeclineWithoutSearchingOrShuffling() {
        harness.setHand(player1, List.of(new OldGrowthDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        setupLibrary(player2);
        List<Card> libraryBefore = List.copyOf(gd.playerDecks.get(player2.getId()));
        int logStart = gd.gameLog.size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, false);
        } else {
            harness.handleCardChosen(player2, -1);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(libraryBefore);
        assertThat(gd.gameLog.subList(logStart, gd.gameLog.size()))
                .noneMatch(entry -> entry.plainText().contains(player2.getUsername() + " searches")
                        || entry.plainText().contains("Library is shuffled"));
    }

    @Test
    @DisplayName("Entering under the nonactive player's control gives the active opponent the search")
    void nonactiveControllerGivesActiveOpponentSearch() {
        setupLibrary(player1);
        setupLibrary(player2);

        harness.enterBattlefieldAndReturn(player2, new OldGrowthDryads());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Plains && permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Plains);
    }

    @Test
    @DisplayName("Finding a land removes it from the library and shuffles the remaining cards")
    void findingLandMovesItAndShuffles() {
        harness.setHand(player1, List.of(new OldGrowthDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        Plains plains = new Plains();
        Forest forest = new Forest();
        RavenousDaggertooth creature = new RavenousDaggertooth();
        harness.setLibrary(player2, List.of(plains, forest, creature));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(forest, creature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(plains.getId()) && permanent.isTapped());
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupLibrary(Player player) {
        harness.setLibrary(player, List.of(new Plains(), new Forest(), new Mountain(), new RavenousDaggertooth()));
    }
}
