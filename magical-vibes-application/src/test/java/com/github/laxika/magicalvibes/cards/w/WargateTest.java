package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WargateTest extends BaseCardTest {

    // ===== Eligibility: any permanent type with MV <= X =====

    @Test
    @DisplayName("Presents permanents of any type with MV <= X, excluding higher-MV and non-permanent cards")
    void presentsPermanentsWithinX() {
        castWargate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // Library: GrizzlyBears (creature MV2), Ornithopter (artifact MV0), Plains (land MV0),
        // AirElemental (creature MV5), Shock (instant MV1).
        // X=2 → permanents with MV <= 2: GrizzlyBears, Ornithopter, Plains.
        assertThat(offeredNames(gd)).containsExactlyInAnyOrder("Grizzly Bears", "Ornithopter", "Plains");
    }

    @Test
    @DisplayName("Excludes permanents with MV greater than X")
    void excludesPermanentsAboveX() {
        castWargate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(offeredNames(gd)).doesNotContain("Air Elemental");
    }

    @Test
    @DisplayName("Excludes non-permanent cards even when their mana value is within X")
    void excludesNonPermanentCards() {
        castWargate(10);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Shock (instant, MV1) is within X=10 but is not a permanent card.
        assertThat(offeredNames(gd)).doesNotContain("Shock");
        assertThat(offeredNames(gd)).containsExactlyInAnyOrder("Grizzly Bears", "Ornithopter", "Plains", "Air Elemental");
    }

    // ===== Destination =====

    @Test
    @DisplayName("Search destination is the battlefield")
    void destinationIsBattlefield() {
        castWargate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Choosing a permanent puts it onto the battlefield")
    void choosingPermanentPutsItOntoBattlefield() {
        castWargate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        String chosenName = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().getFirst().getName();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals(chosenName));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals(chosenName));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Fail to find =====

    @Test
    @DisplayName("Player may fail to find, leaving the battlefield unchanged")
    void failToFind() {
        castWargate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
        int battlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Wargate itself goes to the graveyard =====

    @Test
    @DisplayName("Wargate is put into the graveyard after resolving, not shuffled into the library")
    void wargateGoesToGraveyard() {
        castWargate(2);
        setupLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Wargate"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Wargate"));
    }

    // ===== Helpers =====

    private void castWargate(int xValue) {
        harness.setHand(player1, List.of(new Wargate()));
        // {X}{G}{W}{U}: X generic paid with the extra green.
        harness.addMana(player1, ManaColor.GREEN, xValue + 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, xValue);
    }

    private void setupLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new Ornithopter(), new Plains(), new AirElemental(), new Shock()));
    }

    private List<String> offeredNames(GameData gd) {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
    }
}
