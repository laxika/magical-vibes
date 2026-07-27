package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ConsignToDreamTest extends BaseCardTest {

    private void castOn(UUID targetId) {
        harness.setHand(player1, List.of(new ConsignToDream()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    // ===== Non-red/green permanents: return to hand =====

    @Test
    @DisplayName("Blue permanent is returned to its owner's hand")
    void bluePermanentReturnedToHand() {
        harness.addToBattlefield(player2, new FugitiveWizard()); // Blue
        UUID targetId = harness.getPermanentId(player2, "Fugitive Wizard");
        int deckBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        castOn(targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInHand(player2, "Fugitive Wizard");
        // Not put on library
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
    }

    @Test
    @DisplayName("Colorless permanent is returned to its owner's hand")
    void colorlessPermanentReturnedToHand() {
        harness.addToBattlefield(player2, new Ornithopter()); // Colorless
        UUID targetId = harness.getPermanentId(player2, "Ornithopter");

        castOn(targetId);

        harness.assertInHand(player2, "Ornithopter");
    }

    // ===== Red/green permanents: put on top of library instead =====

    @Test
    @DisplayName("Green permanent is put on top of its owner's library instead")
    void greenPermanentPutOnTopOfLibrary() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // Green
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        int deckBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        castOn(targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Red permanent is put on top of its owner's library instead")
    void redPermanentPutOnTopOfLibrary() {
        harness.addToBattlefield(player2, new HillGiant()); // Red
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        int deckBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        castOn(targetId);

        GameData gd = harness.getGameData();
        harness.assertNotInHand(player2, "Hill Giant");
        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck).hasSize(deckBefore + 1);
        assertThat(deck.getFirst().getName()).isEqualTo("Hill Giant");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        int deckBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new ConsignToDream()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetId);

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Consign to Dream");
    }
}
