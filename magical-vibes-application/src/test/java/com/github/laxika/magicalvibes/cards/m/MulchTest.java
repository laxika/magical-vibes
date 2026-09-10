package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LowlandBasilisk;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mulch.class, VolrathsStronghold.class, LowlandBasilisk.class, Shock.class})
class MulchTest extends BaseCardTest {

    // ===== All lands go to hand =====

    @Test
    @DisplayName("All revealed land cards go to hand")
    void allLandsGoToHand() {
        Card land1 = new VolrathsStronghold();
        Card land2 = new VolrathsStronghold();
        Card land3 = new VolrathsStronghold();
        Card basilisk = new LowlandBasilisk();

        harness.setLibrary(player1, List.of(land1, land2, land3, basilisk));
        harness.castFromHand(player1, new Mulch(), "{1}{G}");

        harness.passBothPriorities();

        // Three lands should be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(land1, land2, land3);
        // Nonland card should be in graveyard
        harness.assertInGraveyard(player1, "Lowland Basilisk");
    }

    // ===== Non-lands go to graveyard =====

    @Test
    @DisplayName("All non-land cards go to graveyard")
    void nonLandsGoToGraveyard() {
        Card shock1 = new Shock();
        Card shock2 = new Shock();
        Card basilisk = new LowlandBasilisk();
        Card land = new VolrathsStronghold();

        harness.setLibrary(player1, List.of(shock1, shock2, basilisk, land));
        harness.castFromHand(player1, new Mulch(), "{1}{G}");

        harness.passBothPriorities();

        // Land goes to hand
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        // Non-lands go to graveyard (plus Mulch itself)
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Lowland Basilisk");
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Shock")).count()).isEqualTo(2);
    }

    // ===== No lands revealed =====

    @Test
    @DisplayName("When no lands are revealed, all cards go to graveyard")
    void noLandsAllToGraveyard() {
        Card shock = new Shock();
        Card basilisk1 = new LowlandBasilisk();
        Card basilisk2 = new LowlandBasilisk();
        Card basilisk3 = new LowlandBasilisk();

        harness.setLibrary(player1, List.of(shock, basilisk1, basilisk2, basilisk3));
        harness.castFromHand(player1, new Mulch(), "{1}{G}");

        harness.passBothPriorities();

        // No lands in hand (hand should be empty since Mulch was cast)
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // All four non-lands + Mulch in graveyard
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Lowland Basilisk");
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Lowland Basilisk")).count()).isEqualTo(3);
    }

    // ===== All lands revealed =====

    @Test
    @DisplayName("When all revealed cards are lands, all go to hand")
    void allLandsRevealed() {
        Card land1 = new VolrathsStronghold();
        Card land2 = new VolrathsStronghold();
        Card land3 = new VolrathsStronghold();
        Card land4 = new VolrathsStronghold();

        harness.setLibrary(player1, List.of(land1, land2, land3, land4));
        harness.castFromHand(player1, new Mulch(), "{1}{G}");

        harness.passBothPriorities();

        // All four lands should be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(land1, land2, land3, land4);
        // Only Mulch itself in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Mulch")).count()).isEqualTo(1);
    }

    // ===== Library smaller than 4 =====

    @Test
    @DisplayName("When library has fewer than 4 cards, reveals all available")
    void librarySmallerThanFour() {
        Card land = new VolrathsStronghold();
        Card basilisk = new LowlandBasilisk();

        harness.setLibrary(player1, List.of(land, basilisk));
        harness.castFromHand(player1, new Mulch(), "{1}{G}");

        harness.passBothPriorities();

        // Land goes to hand, nonland card to graveyard
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        harness.assertInGraveyard(player1, "Lowland Basilisk");
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Does nothing when library is empty")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new Mulch(), "{1}{G}");

        harness.passBothPriorities();

        // Hand should be empty
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== No player interaction required =====

    @Test
    @DisplayName("Effect is deterministic — no player interaction required")
    void noInteractionRequired() {
        Card land = new VolrathsStronghold();
        Card basilisk = new LowlandBasilisk();
        Card shock = new Shock();
        Card secondLand = new VolrathsStronghold();

        harness.setLibrary(player1, List.of(land, basilisk, shock, secondLand));
        harness.castFromHand(player1, new Mulch(), "{1}{G}");

        harness.passBothPriorities();

        // Should not be awaiting any input
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals")).isTrue();
    }

    @Test
    @DisplayName("Cards beyond the top four remain in the library")
    void cardsBeyondTopFourRemainInLibrary() {
        Card land = new VolrathsStronghold();
        Card basilisk = new LowlandBasilisk();
        Card shock = new Shock();
        Card secondBasilisk = new LowlandBasilisk();
        Card untouched = new VolrathsStronghold();

        harness.setLibrary(player1, List.of(land, basilisk, shock, secondBasilisk, untouched));
        harness.castFromHand(player1, new Mulch(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(basilisk, shock, secondBasilisk);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
    }
}
