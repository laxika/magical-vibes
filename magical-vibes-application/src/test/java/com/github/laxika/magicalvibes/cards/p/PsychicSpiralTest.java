package com.github.laxika.magicalvibes.cards.p;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PsychicSpiralTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles controller's graveyard into library and mills target player that many cards")
    void shufflesGraveyardAndMillsThatMany() {
        gd.playerGraveyards.get(player1.getId())
                .addAll(List.of(new Spellbook(), new Spellbook(), new LlanowarElves()));
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        int opponentLibraryBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new PsychicSpiral()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Only Psychic Spiral itself remains — it hits the graveyard after resolving.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore + 3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibraryBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mills nothing when the controller's graveyard is empty")
    void millsNothingWithEmptyGraveyard() {
        int opponentLibraryBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new PsychicSpiral()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibraryBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Targeting yourself shuffles the graveyard away first, then mills into the empty graveyard")
    void canTargetSelf() {
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(new Spellbook(), new Spellbook()));
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new PsychicSpiral()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        // 2 shuffled in, 2 milled back out — the milled cards are the only graveyard contents
        // besides Psychic Spiral itself.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
        Card spiral = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c instanceof PsychicSpiral).findFirst().orElseThrow();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(spiral);
    }
}
