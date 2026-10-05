package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.t.ThoughtScour;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NexusOfFate.class, Counterspell.class, Forest.class, MindRot.class, ThoughtScour.class})
class NexusOfFateTest extends BaseCardTest {

    @Test
    @DisplayName("Takes an extra turn and shuffles itself into its owner's library after resolving")
    void resolvesAndShufflesIntoLibrary() {
        NexusOfFate nexus = new NexusOfFate();
        harness.setHand(player1, List.of(nexus));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0);

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

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId())).contains(nexus);
        harness.assertNotInGraveyard(player2, "Nexus of Fate");
    }

    @Test
    @DisplayName("Countering Nexus shuffles it into its owner's library without granting an extra turn")
    void counteredNexusShufflesWithoutExtraTurn() {
        NexusOfFate nexus = new NexusOfFate();
        harness.setHand(player1, List.of(nexus));
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0);
        harness.castAndResolveInstant(player2, 0, nexus.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1).contains(nexus);
        harness.assertNotInGraveyard(player1, "Nexus of Fate");
        harness.assertInGraveyard(player2, "Counterspell");
    }

    @Test
    @DisplayName("Milling Nexus replaces its graveyard move without milling it again after the shuffle")
    void millingNexusShufflesIntoOwnersLibrary() {
        NexusOfFate nexus = new NexusOfFate();
        Forest forest = new Forest();
        harness.setLibrary(player2, List.of(nexus, forest));
        harness.setHand(player1, List.of(new ThoughtScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nexus);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(forest);
        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting Nexus during an opponent's turn grants its caster the extra turn")
    void opponentTurnStillGrantsCasterExtraTurn() {
        NexusOfFate nexus = new NexusOfFate();
        harness.setHand(player2, List.of(nexus));
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.extraTurns).containsExactly(player2.getId());
        assertThat(gd.playerDecks.get(player2.getId())).contains(nexus);
        harness.assertNotInGraveyard(player2, "Nexus of Fate");
    }
}
