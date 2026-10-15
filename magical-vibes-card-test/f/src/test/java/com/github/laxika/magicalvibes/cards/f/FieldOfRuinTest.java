package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.a.AvenMindcensor;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;

import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieldOfRuin.class, Forest.class, Plains.class, Island.class, Mountain.class,
        FrenziedRaptor.class, AvenMindcensor.class})
class FieldOfRuinTest extends BaseCardTest {

    

    @Test
    @DisplayName("Can tap for colorless mana with first ability")
    void canTapForColorlessMana() {
        harness.addToBattlefield(player1, new FieldOfRuin());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Activating destroy ability sacrifices Field of Ruin and puts ability on stack")
    void activatingSacrificesAndPutsOnStack() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Field of Ruin");

        harness.activateAbility(player1, 0, 1, null, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Field of Ruin");
        harness.assertInGraveyard(player1, "Field of Ruin");
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target own nonbasic land")
    void cannotTargetOwnLand() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = gd.playerBattlefields.get(player1.getId()).get(1).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving destroys target nonbasic land and presents basic land search to active player first")
    void destroysLandAndPresentsSearchToActivePlayerFirst() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Field of Ruin");
        setupLibrary(player1);
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Target land is destroyed
        harness.assertNotOnBattlefield(player2, "Field of Ruin");
        harness.assertInGraveyard(player2, "Field of Ruin");

        // Active player (player1) is prompted to search first (APNAP order)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("After active player searches, opponent is prompted to search")
    void afterActivePlayerSearchesOpponentSearches() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Field of Ruin");
        setupLibrary(player1);
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        // Player 1 (active) picks a basic land
        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);

        // Now player 2 is prompted to search
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
    }

    @Test
    @DisplayName("Both players get basic lands onto the battlefield after searching")
    void bothPlayersGetBasicLands() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Field of Ruin");
        setupLibrary(player1);
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int p1BattlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        int p2BattlefieldBefore = gd.playerBattlefields.get(player2.getId()).size();

        // Player 1 searches
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(p1BattlefieldBefore);

        // Player 2 searches
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(p1BattlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(p2BattlefieldBefore + 1);

        // No more searches pending
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Active player can fail to find, opponent still gets to search")
    void activePlayerFailsToFindOpponentStillSearches() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Field of Ruin");
        setupLibrary(player1);
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Player 1 fails to find
        harness.handleCardChosen(player1, -1);

        // Player 2 still gets to search
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("No search prompt when player has no basic lands in library")
    void noBasicLandsSkipsToNextPlayer() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Field of Ruin");

        // Player 1 has no basic lands in library
        harness.setLibrary(player1, List.of(new FrenziedRaptor(), new FrenziedRaptor()));

        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        // Player 1 is skipped, player 2 gets to search
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new FieldOfRuin());
        UUID targetId = harness.getPermanentId(player2, "Field of Ruin");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Field of Ruin");
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lands found by search enter untapped")
    void searchedLandsEnterUntapped() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Field of Ruin");
        setupLibrary(player1);
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);

        harness.handleCardChosen(player2, 0);

        // The land entered untapped
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND)
                        && p.getCard().getSupertypes().contains(CardSupertype.BASIC)
                        && !p.isTapped());
    }

    @Test
    @DisplayName("Opponent's search respects Aven Mindcensor after the active player searches")
    void opponentSearchIsLimitedToTopFourCards() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player1, new AvenMindcensor());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setLibrary(player2, List.of(new FrenziedRaptor(), new FrenziedRaptor(),
                new FrenziedRaptor(), new FrenziedRaptor(), new Plains()));

        harness.activateAbility(player1, 0, 1, null, harness.getPermanentId(player2, "Field of Ruin"));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player2, "Plains");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("An absent target prevents both players from searching")
    void absentTargetPreventsSearches() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibrary(player1);
        setupLibrary(player2);
        harness.activateAbility(player1, 0, 1, null, harness.getPermanentId(player2, "Field of Ruin"));
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("Cannot target an opponent's nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new FrenziedRaptor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null,
                harness.getPermanentId(player2, "Frenzied Raptor")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setupLibrary(Player player) {
        harness.setLibrary(player, List.of(new Plains(), new Island(), new Mountain(), new FrenziedRaptor()));
    }
}
