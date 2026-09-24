package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NexusOfFate.class, Counterspell.class, Forest.class, MindRot.class})
class NexusOfFateTest extends BaseCardTest {

    @Test
    @DisplayName("Takes an extra turn and shuffles itself into its owner's library after resolving")
    void resolvesAndShufflesIntoLibrary() {
        NexusOfFate nexus = new NexusOfFate();
        harness.setHand(player1, List.of(nexus));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).contains(nexus);
        harness.assertNotInGraveyard(player1, "Nexus of Fate");
    }

    @Test
    @DisplayName("Shuffles itself into its owner's library instead of the graveyard when discarded")
    void replacementEffectAppliesToDiscard() {
        NexusOfFate nexus = new NexusOfFate();
        Forest remainingCard = new Forest();
        harness.setHand(player1, List.of(new MindRot()));
        harness.setHand(player2, List.of(nexus, new Forest(), remainingCard));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId())).contains(nexus);
        harness.assertNotInGraveyard(player2, "Nexus of Fate");
    }
}
