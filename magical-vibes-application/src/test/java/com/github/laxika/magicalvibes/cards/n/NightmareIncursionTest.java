package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightmareIncursion.class, Swamp.class, GrizzlyBears.class, Shock.class})
class NightmareIncursionTest extends BaseCardTest {

    private void castIncursionTargeting(int swamps, Player targetPlayer, List<Card> targetLibrary) {
        for (int i = 0; i < swamps; i++) {
            harness.addToBattlefield(player1, new Swamp());
        }
        harness.setLibrary(targetPlayer, targetLibrary);
        harness.setHand(player1, List.of(new NightmareIncursion()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castSorcery(player1, 0, targetPlayer.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Exiles up to X cards where X is the number of Swamps you control")
    void exilesUpToSwampCount() {
        castIncursionTargeting(3, player2,
                List.of(new GrizzlyBears(), new Shock(), new Swamp(), new GrizzlyBears()));

        // Search allows exiling up to 3 cards; pick three (each pick re-presents from index 0)
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Exiles only as many as the library holds when it is smaller than X")
    void exilesAllWhenLibrarySmallerThanX() {
        castIncursionTargeting(3, player2, List.of(new GrizzlyBears(), new Shock()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("With no Swamps X is zero: exiles nothing and no search is offered")
    void noSwampsExilesNothing() {
        castIncursionTargeting(0, player2, List.of(new GrizzlyBears(), new Shock()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInGraveyard(player1, "Nightmare Incursion");
    }

    @Test
    @DisplayName("Can target yourself, exiling from your own library")
    void canTargetSelf() {
        castIncursionTargeting(2, player1, List.of(new GrizzlyBears(), new Shock(), new GrizzlyBears()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("May choose fewer than X cards")
    void mayChooseFewerThanSwampCount() {
        castIncursionTargeting(3, player2, List.of(new GrizzlyBears(), new Shock()));

        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Counts only Swamps controlled by the spell's controller")
    void ignoresSwampsControlledByTargetPlayer() {
        harness.addToBattlefield(player2, new Swamp());
        castIncursionTargeting(1, player2, List.of(new GrizzlyBears(), new Shock()));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
    @Test
    @DisplayName("May stop searching after choosing fewer than X cards")
    void mayStopAfterOneCard() {
        Card chosen = new Shock();
        Card remaining = new GrizzlyBears();
        Card land = new Swamp();
        castIncursionTargeting(3, player2, List.of(remaining, chosen, land));

        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(remaining, land);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2).doesNotContain(chosen);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInGraveyard(player1, "Nightmare Incursion");
    }

    @Test
    @DisplayName("An empty target library finishes without a choice")
    void emptyLibraryFinishesSearch() {
        castIncursionTargeting(2, player2, List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInGraveyard(player1, "Nightmare Incursion");
    }

    @Test
    @DisplayName("Counts Swamps at resolution rather than when cast")
    void countsSwampsAtResolution() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Shock(), new Swamp()));
        harness.setHand(player1, List.of(new NightmareIncursion()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castSorcery(player1, 0, player2.getId());

        harness.addToBattlefield(player1, new Swamp());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }
}
