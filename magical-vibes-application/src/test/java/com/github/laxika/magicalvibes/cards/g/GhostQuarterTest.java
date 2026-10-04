package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
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

@CardUsed({GhostQuarter.class, Forest.class, Island.class, Mountain.class, Plains.class,
        DoomedTraveler.class})
class GhostQuarterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating destroy ability sacrifices Ghost Quarter and puts ability on stack")
    void activatingSacrificesAndPutsOnStack() {
        harness.addToBattlefield(player1, new GhostQuarter());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        harness.activateAbility(player1, 0, 1, null, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Ghost Quarter");
        harness.assertInGraveyard(player1, "Ghost Quarter");
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving destroys target land and presents basic land search to its controller")
    void destroysLandAndPresentsSearch() {
        harness.addToBattlefield(player1, new GhostQuarter());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Target land is destroyed
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");

        // Player 2 (the land's controller) is prompted to search
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Land's controller can choose a basic land to put onto the battlefield untapped")
    void controllerChoosesBasicLandUntapped() {
        harness.addToBattlefield(player1, new GhostQuarter());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int battlefieldBefore = gd.playerBattlefields.get(player2.getId()).size();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(battlefieldBefore + 1);
        // The chosen land enters untapped
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND)
                        && p.getCard().getSupertypes().contains(CardSupertype.BASIC)
                        && !p.isTapped());
    }

    @Test
    @DisplayName("Land's controller can fail to find (may search)")
    void controllerCanFailToFind() {
        harness.addToBattlefield(player1, new GhostQuarter());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        setupLibrary(player2);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleCardChosen(player2, -1);

        // No new land on battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Plains")
                        || p.getCard().getName().equals("Island")
                        || p.getCard().getName().equals("Mountain"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can target own land")
    void canTargetOwnLand() {
        harness.addToBattlefield(player1, new GhostQuarter());
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();
        setupLibrary(player1);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Own land is destroyed
        harness.assertInGraveyard(player1, "Forest");

        // Player 1 is prompted to search their own library
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new GhostQuarter());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DoomedTraveler()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ghost Quarter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Controller can decline searching even when the library has no basic lands")
    void noBasicLandsStillAllowsDecliningSearch() {
        harness.addToBattlefield(player1, new GhostQuarter());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        // Set up library with no basic lands
        harness.setLibrary(player2, List.of(new DoomedTraveler(), new DoomedTraveler()));

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isFalse();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new GhostQuarter());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        GameData gd = harness.getGameData();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can tap for colorless mana with first ability")
    void canTapForColorlessMana() {
        harness.addToBattlefield(player1, new GhostQuarter());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Controller can decline the search without shuffling")
    void controllerCanDeclineSearchWithoutShuffling() {
        harness.addToBattlefield(player1, new GhostQuarter());
        var target = harness.addToBattlefieldAndReturn(player2, new Forest());
        var plains = new Plains();
        var island = new Island();
        var mountain = new Mountain();
        harness.setLibrary(player2, List.of(plains, island, mountain));

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(plains, island, mountain);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled")).isFalse();
    }

    @Test
    @DisplayName("Targeting Ghost Quarter itself sacrifices it and does not search")
    void targetingSelfDoesNotSearch() {
        var quarter = harness.addToBattlefieldAndReturn(player1, new GhostQuarter());
        var plains = new Plains();
        var island = new Island();
        harness.setLibrary(player1, List.of(plains, island));

        harness.activateAbility(player1, 0, 1, null, quarter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghost Quarter");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains, island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Regeneration saves the land but its controller can still find a basic land")
    void regeneratedLandStillAllowsSearch() {
        harness.addToBattlefield(player1, new GhostQuarter());
        var target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.setRegenerationShield(1);
        harness.setLibrary(player2, List.of(new Plains()));

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
        harness.assertOnBattlefield(player2, "Plains");
    }

    @Test
    @DisplayName("Nonbasic lands and creatures are excluded from the basic land search")
    void searchExcludesNonbasicLandsAndCreatures() {
        harness.addToBattlefield(player1, new GhostQuarter());
        var target = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());
        var nonbasic = new GhostQuarter();
        var creature = new DoomedTraveler();
        var plains = new Plains();
        harness.setLibrary(player2, List.of(nonbasic, creature, plains));

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(plains);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Plains");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(nonbasic, creature);
    }

    private void setupLibrary(Player player) {
        harness.setLibrary(player, List.of(new Plains(), new Island(), new Mountain(), new DoomedTraveler()));
    }
}
