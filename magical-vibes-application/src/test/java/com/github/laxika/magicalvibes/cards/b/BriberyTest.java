package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bribery.class, GrizzlyBears.class, Island.class, Unsummon.class})
class BriberyTest extends BaseCardTest {

    private void prepareBribery() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new Bribery()));
        harness.addMana(player1, ManaColor.BLUE, 5); // {3}{U}{U}
    }

    private void castBribery() {
        prepareBribery();
        harness.castSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetItsController() {
        prepareBribery();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only creature cards from the target opponent's library are offered")
    void offersOnlyCreatures() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Unsummon(), new Island()));

        castBribery();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().cards()).extracting("name").containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Chosen creature enters under the caster's control and leaves the shuffled library")
    void putsChosenCreatureUnderControl() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Island()));

        castBribery();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        // The stolen creature is on player1's battlefield.
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        // It left the opponent's library and did not go under their control.
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Bribery reaches the battlefield and nothing else — not exile, not either graveyard.
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gameLogContains(gd.playerIdToName.get(player2.getId()) + "'s library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("A creature put onto the battlefield under your control remains owned by the opponent")
    void chosenCreatureReturnsToItsOwner() {
        GrizzlyBears stolen = new GrizzlyBears();
        stolen.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(stolen, new Island()));

        castBribery();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        UUID stolenPermanentId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, stolenPermanentId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining to find leaves the creature in the library")
    void decliningLeavesCreatureInLibrary() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Island()));

        castBribery();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gameLogContains("chooses not to take a card. Library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("No creatures in the target library puts nothing onto the battlefield")
    void noCreaturesFindsNothing() {
        harness.setLibrary(player2, List.of(new Unsummon(), new Island()));

        castBribery();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("searches " + gd.playerIdToName.get(player2.getId())
                + "'s library but finds no matching cards. Library is shuffled.")).isTrue();
    }
}
