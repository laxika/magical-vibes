package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViciousRumors.class, Forest.class, GrizzlyBears.class})
class ViciousRumorsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage, makes each opponent discard and mill, then gains life")
    void resolvesAllEffectsInOrder() {
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(new ViciousRumors()));
        harness.setHand(player2, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An empty hand does not stop milling or gaining life")
    void emptyHandDoesNotStopRemainingEffects() {
        ViciousRumors milled = new ViciousRumors();
        ViciousRumors remaining = new ViciousRumors();
        harness.setHand(player1, List.of(new ViciousRumors()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(milled, remaining));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(milled);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent discarding or gaining life")
    void emptyLibraryDoesNotStopLifeGain() {
        ViciousRumors discarded = new ViciousRumors();
        harness.setHand(player1, List.of(new ViciousRumors()));
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent chooses one discard and the controller keeps their hand and library")
    void opponentChoosesDiscardAndControllerIsUnaffected() {
        ViciousRumors retained = new ViciousRumors();
        ViciousRumors discarded = new ViciousRumors();
        ViciousRumors milled = new ViciousRumors();
        ViciousRumors controllerHand = new ViciousRumors();
        ViciousRumors controllerLibrary = new ViciousRumors();
        harness.setHand(player1, List.of(new ViciousRumors(), controllerHand));
        harness.setHand(player2, List.of(retained, discarded));
        harness.setLibrary(player1, List.of(controllerLibrary));
        harness.setLibrary(player2, List.of(milled));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(milled);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded, milled);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerHand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerLibrary);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }
}
