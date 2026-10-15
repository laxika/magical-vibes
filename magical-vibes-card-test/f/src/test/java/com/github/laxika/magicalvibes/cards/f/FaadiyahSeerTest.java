package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DromarsCavern;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaadiyahSeer.class, DromarsCavern.class, Ornithopter.class})
class FaadiyahSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Only the drawn copy is discarded when another copy is already in hand")
    void discardsOnlyDrawnCopy() {
        addCreatureReady(player1, new FaadiyahSeer());
        Ornithopter existing = new Ornithopter();
        Ornithopter drawn = new Ornithopter();
        harness.setHand(player1, List.of(existing));
        harness.setLibrary(player1, List.of(drawn, new DromarsCavern()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(existing);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.cardsDrawnThisTurnIds.get(player1.getId())).containsExactly(drawn.getId());
    }

    @Test
    @DisplayName("Activation taps the Seer immediately and draws only on resolution")
    void tapCostIsPaidBeforeDraw() {
        var seer = addCreatureReady(player1, new FaadiyahSeer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new DromarsCavern()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(seer.isTapped()).isTrue();
        harness.assertNotInHand(player1, "Dromar's Cavern");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Dromar's Cavern");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the tap ability")
    void summoningSicknessPreventsActivation() {
        var seer = harness.addToBattlefieldAndReturn(player1, new FaadiyahSeer());
        seer.setSummoningSick(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new DromarsCavern()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(seer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertNotInHand(player1, "Dromar's Cavern");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

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
