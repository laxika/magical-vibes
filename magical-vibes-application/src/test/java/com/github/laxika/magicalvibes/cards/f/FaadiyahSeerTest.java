package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DromarsCavern;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaadiyahSeer.class, DromarsCavern.class, Ornithopter.class})
class FaadiyahSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Drawn land card is kept in hand")
    void drawnLandIsKept() {
        addCreatureReady(player1, new FaadiyahSeer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new DromarsCavern()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Dromar's Cavern");
        harness.assertNotInGraveyard(player1, "Dromar's Cavern");
        assertThat(gameLogContains("reveals")).isTrue();
    }

    @Test
    @DisplayName("Drawn nonland card is revealed and discarded")
    void drawnNonlandIsDiscarded() {
        addCreatureReady(player1, new FaadiyahSeer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Ornithopter()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(gameLogContains("reveals")).isTrue();
    }

    @Test
    @DisplayName("Empty library does not discard an existing hand card")
    void emptyLibraryDoesNotDiscard() {
        addCreatureReady(player1, new FaadiyahSeer());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }
}
