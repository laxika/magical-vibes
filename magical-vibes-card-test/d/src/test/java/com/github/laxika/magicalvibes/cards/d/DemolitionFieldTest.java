package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemolitionField.class, Forest.class, Plains.class, Island.class, Mountain.class})
class DemolitionFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Can tap for colorless mana")
    void canTapForColorlessMana() {
        harness.addToBattlefield(player1, new DemolitionField());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Activating the destroy ability sacrifices Demolition Field")
    void activatingSacrificesAndPutsOnStack() {
        harness.addToBattlefield(player1, new DemolitionField());
        harness.addToBattlefield(player2, new DemolitionField());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Demolition Field");

        harness.activateAbility(player1, 0, 1, null, targetId);

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Demolition Field");
        harness.assertInGraveyard(player1, "Demolition Field");
        assertThat(gameData.stack).hasSize(1);
        StackEntry entry = gameData.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Cannot target a basic land or an own nonbasic land")
    void targetMustBeNonbasicLandOpponentControls() {
        harness.addToBattlefield(player1, new DemolitionField());
        harness.addToBattlefield(player1, new DemolitionField());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID ownLandId = harness.getPermanentId(player1, "Demolition Field");
        UUID basicLandId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, ownLandId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, basicLandId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys an opponent's nonbasic land and lets both players search for a basic land")
    void destroysLandAndBothPlayersSearch() {
        harness.addToBattlefield(player1, new DemolitionField());
        harness.addToBattlefield(player2, new DemolitionField());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Demolition Field");
        setupLibrary(player1);
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertInGraveyard(player2, "Demolition Field");
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player2.getId());
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        harness.handleCardChosen(player2, 0);
        harness.assertOnBattlefield(player2, "Plains");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().isTapped()).isFalse();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An absent target stops both library searches")
    void absentTargetStopsSearches() {
        harness.addToBattlefield(player1, new DemolitionField());
        harness.addToBattlefield(player2, new DemolitionField());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibrary(player1);
        setupLibrary(player2);
        UUID targetId = harness.getPermanentId(player2, "Demolition Field");

        harness.activateAbility(player1, 0, 1, null, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("Cannot activate the destroy ability without two mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new DemolitionField());
        harness.addToBattlefield(player2, new DemolitionField());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player2, "Demolition Field");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Demolition Field");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate the destroy ability after tapping for mana")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new DemolitionField());
        harness.addToBattlefield(player2, new DemolitionField());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Demolition Field");
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Demolition Field");
        assertThat(gd.stack).isEmpty();
    }

    private void setupLibrary(Player player) {
        harness.setLibrary(player, List.of(new Plains(), new Island(), new Mountain()));
    }
}
