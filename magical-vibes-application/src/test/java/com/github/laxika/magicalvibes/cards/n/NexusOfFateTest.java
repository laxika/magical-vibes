package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NexusOfFate.class, Counterspell.class})
class NexusOfFateTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Nexus of Fate gives its controller an extra turn and shuffles it into the library")
    void resolvesExtraTurnAndShufflesIntoLibrary() {
        NexusOfFate nexus = new NexusOfFate();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, nexus, "{5}{U}{U}");

        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        harness.assertNotInGraveyard(player1, "Nexus of Fate");
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(librarySizeBefore + 1)
                .anyMatch(card -> card.getId().equals(nexus.getId()));
    }

    @Test
    @DisplayName("Countering Nexus of Fate shuffles it into its owner's library without giving an extra turn")
    void counteredSpellShufflesIntoLibraryWithoutExtraTurn() {
        NexusOfFate nexus = new NexusOfFate();
        harness.setHand(player1, List.of(nexus));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, nexus.getId());
        harness.passBothPriorities();

        assertThat(gd.extraTurns).isEmpty();
        harness.assertNotInGraveyard(player1, "Nexus of Fate");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(nexus.getId()));
    }
}
