package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrimalSurgeTest extends BaseCardTest {

    private void castPrimalSurge(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new PrimalSurge()));
        harness.addMana(player1, ManaColor.GREEN, 10);

        harness.castAndResolveInstant(player1, 0);
    }

    private List<String> battlefieldNames() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard().getName())
                .toList();
    }

    private List<String> exiledNames() {
        return gd.exiledCards.stream().map(entry -> entry.card().getName()).toList();
    }

    @Test
    @DisplayName("A permanent card prompts, and accepting puts it onto the battlefield and exiles the next card")
    void permanentCardMayBePutOntoBattlefield() {
        castPrimalSurge(new ArrayList<>(List.of(new GrizzlyBears(), new Shock())));

        // Grizzly Bears is exiled and awaiting the put decision; nothing on the battlefield yet.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(battlefieldNames()).isEmpty();

        harness.handleMayAbilityChosen(player1, true);

        // The Bears entered, then Shock (a nonpermanent card) was exiled and ended the process.
        assertThat(battlefieldNames()).containsExactly("Grizzly Bears");
        assertThat(exiledNames()).containsExactly("Shock");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The process repeats for as long as the controller keeps accepting")
    void repeatsWhilePermanentsAreAccepted() {
        castPrimalSurge(new ArrayList<>(List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Shock())));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(battlefieldNames()).containsExactly("Grizzly Bears", "Forest", "Grizzly Bears");
        assertThat(exiledNames()).containsExactly("Shock");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Declining ends the process and leaves the exiled permanent card in exile")
    void decliningLeavesTheCardInExile() {
        castPrimalSurge(new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(battlefieldNames()).isEmpty();
        assertThat(exiledNames()).containsExactly("Grizzly Bears");
        // The second Bears was never exiled — the process stopped.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A nonpermanent top card is exiled and ends the process with no prompt")
    void nonpermanentTopCardEndsProcessImmediately() {
        castPrimalSurge(new ArrayList<>(List.of(new Shock(), new GrizzlyBears())));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(exiledNames()).containsExactly("Shock");
        assertThat(battlefieldNames()).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The process stops once the library empties")
    void stopsWhenLibraryEmpties() {
        castPrimalSurge(new ArrayList<>(List.of(new GrizzlyBears())));

        harness.handleMayAbilityChosen(player1, true);

        assertThat(battlefieldNames()).containsExactly("Grizzly Bears");
        assertThat(exiledNames()).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library exiles nothing and prompts nothing")
    void emptyLibraryDoesNothing() {
        castPrimalSurge(new ArrayList<>());

        assertThat(gd.exiledCards).isEmpty();
        assertThat(battlefieldNames()).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
