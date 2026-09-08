package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AltarOfTheBroodTest extends BaseCardTest {

    @Test
    @DisplayName("Another permanent entering under the controller's control makes each opponent mill one")
    void ownPermanentEnteringMillsOpponent() {
        harness.addToBattlefield(player1, new AltarOfTheBrood());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A permanent entering under an opponent's control does not trigger it")
    void opponentPermanentEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new AltarOfTheBrood());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Altar of the Brood does not trigger for its own entry")
    void ownEntryDoesNotTrigger() {
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new AltarOfTheBrood()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
