package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AprilONeilHacktivist.class, ChromaticStar.class, GrizzlyBears.class, Shock.class})
class AprilONeilHacktivistTest extends BaseCardTest {

    @Test
    @DisplayName("Draws once for each distinct card type among spells cast this turn")
    void drawsForEachDistinctSpellCardType() {
        harness.addToBattlefield(player1, new AprilONeilHacktivist());
        harness.setHand(player1, List.of(new Shock(), new GrizzlyBears(), new ChromaticStar()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        setDeck(List.of(new Shock(), new Shock(), new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        int handBeforeTrigger = gd.playerHands.get(player1.getId()).size();
        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeTrigger + 3);
    }

    @Test
    @DisplayName("Counts a card type only once when multiple spells share it")
    void countsEachCardTypeOnlyOnce() {
        harness.addToBattlefield(player1, new AprilONeilHacktivist());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        setDeck(List.of(new Shock(), new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        int handBeforeTrigger = gd.playerHands.get(player1.getId()).size();
        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeTrigger + 1);
    }

    @Test
    @DisplayName("Does not draw when no spell was cast this turn")
    void doesNotDrawWithoutSpells() {
        harness.addToBattlefield(player1, new AprilONeilHacktivist());
        setDeck(List.of(new Shock()));

        int handBeforeTrigger = gd.playerHands.get(player1.getId()).size();
        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeTrigger);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setDeck(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
