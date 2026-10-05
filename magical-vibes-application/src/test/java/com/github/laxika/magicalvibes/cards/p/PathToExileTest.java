package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PathToExile.class, GrizzlyBears.class, Forest.class, Island.class, Mountain.class})
class PathToExileTest extends BaseCardTest {

    private void givePath() {
        harness.setHand(player1, List.of(new PathToExile()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    @Test
    @DisplayName("Exiles the target creature and prompts its controller to search for a basic land (tapped)")
    void exilesCreatureAndPresentsTappedSearch() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        setupLibrary(player2);
        givePath();

        harness.castAndResolveInstant(player1, 0, target.getId());

        // Removed from battlefield and put into exile — not the graveyard.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));

        // The exiled creature's controller (player2) is the one offered the tapped basic-land search.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Exiled creature's controller puts the chosen basic land onto the battlefield tapped")
    void chosenLandEntersTapped() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        setupLibrary(player2);
        givePath();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND)
                        && p.getCard().getSupertypes().contains(CardSupertype.BASIC)
                        && p.isTapped());
    }

    @Test
    @DisplayName("Search routes to the caster when exiling the caster's own creature")
    void searchRoutesToCasterForOwnCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        setupLibrary(player1);
        givePath();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        givePath();

        var landId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining the optional search does not search or shuffle")
    void decliningSearchDoesNotShuffle() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        setupLibrary(player2);
        givePath();

        harness.castAndResolveInstant(player1, 0, target.getId());
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player2, false);
        } else {
            harness.handleCardChosen(player2, -1);
        }

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.gameLog).noneMatch(entry ->
                entry.plainText().contains("searches their library")
                        || entry.plainText().contains("Library is shuffled"));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A target that leaves before resolution grants no search")
    void missingTargetGrantsNoSearch() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        setupLibrary(player2);
        givePath();

        harness.castInstant(player1, 0, target.getId());
        harness.getPermanentRemovalService().removePermanentToExile(gd, target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Island", "Mountain", "Grizzly Bears");
        harness.assertInGraveyard(player1, "Path to Exile");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void setupLibrary(Player player) {
        harness.setLibrary(player, List.of(new Island(), new Mountain(), new GrizzlyBears()));
    }
}
