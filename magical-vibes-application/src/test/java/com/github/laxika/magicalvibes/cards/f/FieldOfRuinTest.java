package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldOfRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Field of Ruin has correct abilities")
    void hasCorrectAbilities() {
        FieldOfRuin card = new FieldOfRuin();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // First ability: {T}: Add {C}
        var manaAbility = card.getActivatedAbilities().get(0);
        assertThat(manaAbility.isRequiresTap()).isTrue();
        assertThat(manaAbility.getManaCost()).isNull();
        assertThat(manaAbility.getEffects()).hasSize(1);
        assertThat(manaAbility.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);

        // Second ability: {2}, {T}, Sacrifice: Destroy target nonbasic land...
        var destroyAbility = card.getActivatedAbilities().get(1);
        assertThat(destroyAbility.isRequiresTap()).isTrue();
        assertThat(destroyAbility.getManaCost()).isEqualTo("{2}");
        assertThat(destroyAbility.getEffects()).hasSize(2);
        assertThat(destroyAbility.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(destroyAbility.getEffects().get(1))
                .isInstanceOf(DestroyTargetAndEachPlayerSearchesBasicLandToBattlefieldEffect.class);
    }

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
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");

        harness.activateAbility(player1, 0, 1, null, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Field of Ruin"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Field of Ruin"));
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
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player1, "Ghost Quarter");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving destroys target nonbasic land and presents basic land search to active player first")
    void destroysLandAndPresentsSearchToActivePlayerFirst() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");
        setupLibrary(player1);
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Target land is destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ghost Quarter"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Ghost Quarter"));

        // Active player (player1) is prompted to search first (APNAP order)
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.librarySearch().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("After active player searches, opponent is prompted to search")
    void afterActivePlayerSearchesOpponentSearches() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");
        setupLibrary(player1);
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        // Player 1 (active) picks a basic land
        GameData gd = harness.getGameData();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Now player 2 is prompted to search
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.librarySearch().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
    }

    @Test
    @DisplayName("Both players get basic lands onto the battlefield after searching")
    void bothPlayersGetBasicLands() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");
        setupLibrary(player1);
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int p1BattlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        int p2BattlefieldBefore = gd.playerBattlefields.get(player2.getId()).size();

        // Player 1 searches
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(p1BattlefieldBefore + 1);

        // Player 2 searches
        harness.getGameService().handleLibraryCardChosen(gd, player2, 0);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(p2BattlefieldBefore + 1);

        // No more searches pending
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
    }

    @Test
    @DisplayName("Active player can fail to find, opponent still gets to search")
    void activePlayerFailsToFindOpponentStillSearches() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");
        setupLibrary(player1);
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Player 1 fails to find
        harness.getGameService().handleLibraryCardChosen(gd, player1, -1);

        // Player 2 still gets to search
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("No search prompt when player has no basic lands in library")
    void noBasicLandsSkipsToNextPlayer() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");

        // Player 1 has no basic lands in library
        List<Card> deck1 = harness.getGameData().playerDecks.get(player1.getId());
        deck1.clear();
        deck1.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        // Player 1 is skipped, player 2 gets to search
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new GhostQuarter());
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lands found by search enter untapped")
    void searchedLandsEnterUntapped() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");
        setupLibrary(player1);
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // The land entered untapped
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND)
                        && p.getCard().getSupertypes().contains(CardSupertype.BASIC)
                        && !p.isTapped());
    }

    private void setupLibrary(Player player) {
        List<Card> deck = harness.getGameData().playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Island(), new Mountain(), new GrizzlyBears()));
    }
}
