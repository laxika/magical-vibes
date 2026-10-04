package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.ExileToOwnerGraveyardAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrinningTotem.class, Swamp.class, GrizzlyBears.class, Fog.class})
class GrinningTotemTest extends BaseCardTest {

    private Permanent addReadyGrinningTotem() {
        Permanent totem = harness.addToBattlefieldAndReturn(player1, new GrinningTotem());
        totem.setSummoningSick(false);
        return totem;
    }

    private void activateGrinningTotem() {
        addReadyGrinningTotem();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Ability cannot target its controller")
    void cannotTargetController() {
        addReadyGrinningTotem();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grinning Totem");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chosen card keeps its owner in exile with play permission and cleanup scheduled")
    void exilesChosenCardWithPlayPermissionAndSchedulesCleanup() {
        Card swamp = new Swamp();
        harness.setLibrary(player2, List.of(swamp));

        activateGrinningTotem();
        harness.handleCardChosen(player1, 0);

        // Exile preserves ownership; the searching player receives play permission.
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(swamp.getId()));
        assertThat(gd.findExiledCard(swamp.getId()).faceDown()).isFalse();
        assertThat(gd.exilePlayPermissions.get(swamp.getId())).isEqualTo(player1.getId());
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(swamp.getId()));
        assertThat(gameLogContains("Library is shuffled.")).isTrue();

        // Cleanup is scheduled for the caster's next upkeep, targeting the true owner's graveyard.
        List<ExileToOwnerGraveyardAtNextUpkeep> scheduled =
                gd.getDelayedActions(ExileToOwnerGraveyardAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().ownerId()).isEqualTo(player2.getId());
        assertThat(scheduled.getFirst().cardId()).isEqualTo(swamp.getId());

        // Grinning Totem was sacrificed as a cost.
        harness.assertNotOnBattlefield(player1, "Grinning Totem");
        harness.assertInGraveyard(player1, "Grinning Totem");
    }

    @Test
    @DisplayName("Unplayed exiled card is put into its owner's graveyard at the caster's next upkeep")
    void unplayedCardGoesToOwnerGraveyardAtCasterUpkeep() {
        Card swamp = new Swamp();
        harness.setLibrary(player2, List.of(swamp));

        activateGrinningTotem();
        harness.handleCardChosen(player1, 0);

        // Caster's next upkeep.
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Card leaves exile, loses permission, and enters its owner's (player2's) graveyard.
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getId().equals(swamp.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(swamp.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(swamp.getId()));
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cleanup does not fire on an opponent's upkeep — permission lasts until the caster's upkeep")
    void cleanupDoesNotFireOnOpponentUpkeep() {
        Card swamp = new Swamp();
        harness.setLibrary(player2, List.of(swamp));

        activateGrinningTotem();
        harness.handleCardChosen(player1, 0);

        // Opponent's upkeep — not "your next upkeep".
        advanceToUpkeep(player2);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(swamp.getId()));
        assertThat(gd.exilePlayPermissions.get(swamp.getId())).isEqualTo(player1.getId());
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("A card that was played is not put into the graveyard at the caster's upkeep")
    void playedCardIsNotPutIntoGraveyard() {
        Card swamp = new Swamp();
        harness.setLibrary(player2, List.of(swamp));

        activateGrinningTotem();
        harness.handleCardChosen(player1, 0);

        // Caster plays the exiled land.
        harness.castFromExile(player1, swamp.getId());
        harness.assertOnBattlefield(player1, "Swamp");

        // The caster's upkeep cleanup finds nothing to move — the card stays on the battlefield.
        advanceToUpkeep(player1);

        harness.assertOnBattlefield(player1, "Swamp");
        assertThat(gd.playerGraveyards.getOrDefault(player2.getId(), List.of()))
                .noneMatch(c -> c.getId().equals(swamp.getId()));
    }

    @Test
    @DisplayName("A nonland exiled card can be cast before the caster's next upkeep")
    void castsNonlandCardBeforeCleanup() {
        Card grizzlyBears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(grizzlyBears));

        activateGrinningTotem();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, grizzlyBears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exilePlayPermissions).doesNotContainKey(grizzlyBears.getId());

        advanceToUpkeep(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.getOrDefault(player2.getId(), List.of()))
                .noneMatch(c -> c.getId().equals(grizzlyBears.getId()));
    }

    @Test
    @DisplayName("A cast spell returns to its owner's graveyard")
    void castSpellReturnsToOwnerGraveyard() {
        Card fog = new Fog();
        harness.setLibrary(player2, List.of(fog));

        activateGrinningTotem();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, fog.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(fog.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(fog.getId()));
    }

    @Test
    @DisplayName("An empty target library produces no exiled card or cleanup")
    void emptyTargetLibraryProducesNoCleanup() {
        harness.setLibrary(player2, List.of());

        activateGrinningTotem();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gameLogContains("searches " + player2.getUsername()
                + "'s library but it is empty. Library is shuffled.")).isTrue();
        assertThat(gd.getDelayedActions(ExileToOwnerGraveyardAtNextUpkeep.class)).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grinning Totem");
        harness.assertInGraveyard(player1, "Grinning Totem");
    }

    @Test
    @DisplayName("A nonempty target library cannot decline the search")
    void cannotDeclineToFindCard() {
        Card swamp = new Swamp();
        harness.setLibrary(player2, List.of(swamp));

        activateGrinningTotem();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(swamp);
    }

    @Test
    @DisplayName("Pays {2} and sacrifices itself to activate")
    void paysActivationCostAndSacrificesSelf() {
        addReadyGrinningTotem();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player1, "Grinning Totem");
        harness.assertInGraveyard(player1, "Grinning Totem");
    }

    @Test
    @DisplayName("Upkeep cleanup uses the stack after play permission expires")
    void cleanupWaitsForDelayedTriggerToResolve() {
        Card swamp = new Swamp();
        harness.setLibrary(player2, List.of(swamp));

        activateGrinningTotem();
        harness.handleCardChosen(player1, 0);
        advanceToUpkeep(player1);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(swamp.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(swamp);
        harness.assertNotInGraveyard(player2, "Swamp");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Swamp");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(swamp);
    }

    @Test
    @DisplayName("Play permission does not waive the stolen spell's mana cost")
    void cannotCastWithoutPayingManaCost() {
        Card bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears));
        activateGrinningTotem();
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The opponent cannot use the searching player's play permission")
    void ownerCannotPlayCardUsingTotemPermission() {
        Card fog = new Fog();
        harness.setLibrary(player2, List.of(fog));
        activateGrinningTotem();
        harness.handleCardChosen(player1, 0);
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player2, fog.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(fog);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent totem = addReadyGrinningTotem();
        totem.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grinning Totem");
        harness.assertNotInGraveyard(player1, "Grinning Totem");
    }
}
