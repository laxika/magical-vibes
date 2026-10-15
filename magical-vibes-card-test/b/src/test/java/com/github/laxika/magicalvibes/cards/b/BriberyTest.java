package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bribery.class, Forest.class, GrizzlyBears.class, Island.class, RavenousRats.class, Unsummon.class})
class BriberyTest extends BaseCardTest {

    private void castBribery() {
        castBribery(player2.getId());
    }

    private void castBribery(UUID targetPlayerId) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new Bribery()));
        harness.addMana(player1, ManaColor.BLUE, 5); // {3}{U}{U}
        harness.castSorcery(player1, 0, targetPlayerId);
    }

    @Test
    @DisplayName("Only creature cards from the target opponent's library are offered")
    void offersOnlyCreatures() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));

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
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));

        castBribery();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        // The stolen creature is on player1's battlefield.
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        // It left the opponent's library and did not go under their control.
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // The chosen creature enters the battlefield rather than exile or a graveyard.
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gameLogContains(player2.getUsername() + "'s library is shuffled.")).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Declining to find leaves the creature in the library")
    void decliningLeavesCreatureInLibrary() {
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));

        castBribery();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("No creatures in the target library puts nothing onto the battlefield")
    void noCreaturesFindsNothing() {
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        castBribery();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Bribery can target only an opponent")
    void cannotTargetYourself() {
        assertThatThrownBy(() -> castBribery(player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private void prepareBribery() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new Bribery()));
        harness.addMana(player1, ManaColor.BLUE, 5); // {3}{U}{U}
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetItsController() {
        prepareBribery();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
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
        harness.castAndResolveInstant(player1, 0, stolenPermanentId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty opponent library resolves without a choice")
    void emptyLibraryFindsNothing() {
        harness.setLibrary(player2, List.of());

        castBribery();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Bribery");
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("Only the selected creature moves, leaving the other creature in the opponent's library")
    void takesOnlyOneCreature() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Forest land = new Forest();
        harness.setLibrary(player2, List.of(first, second, land));

        castBribery();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(second.getId());
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(card -> card.getId()).containsExactlyInAnyOrder(first.getId(), land.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The stolen creature's enters trigger is controlled by the caster")
    void entersTriggerUsesNewController() {
        harness.setLibrary(player2, List.of(new RavenousRats(), new Forest()));
        harness.setHand(player2, List.of(new Island()));

        castBribery();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Ravenous Rats");
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Island");
        harness.assertNotInHand(player2, "Island");
    }
}
