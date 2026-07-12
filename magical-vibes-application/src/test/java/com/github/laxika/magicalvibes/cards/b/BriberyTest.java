package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BriberyTest extends BaseCardTest {

    private void castBribery() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new Bribery()));
        harness.addMana(player1, ManaColor.BLUE, 5); // {3}{U}{U}
        harness.castSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Only creature cards from the target opponent's library are offered")
    void offersOnlyCreatures() {
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(new GrizzlyBears(), new Divination(), new Forest()));

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
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(new GrizzlyBears(), new Forest()));

        castBribery();
        harness.passBothPriorities();

        gs.handleLibraryCardChosen(gd, player1, 0);

        // The stolen creature is on player1's battlefield.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // It left the opponent's library and did not go under their control.
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Declining to find leaves the creature in the library")
    void decliningLeavesCreatureInLibrary() {
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(new GrizzlyBears(), new Forest()));

        castBribery();
        harness.passBothPriorities();

        gs.handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("No creatures in the target library puts nothing onto the battlefield")
    void noCreaturesFindsNothing() {
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).addAll(List.of(new Divination(), new Forest()));

        castBribery();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
