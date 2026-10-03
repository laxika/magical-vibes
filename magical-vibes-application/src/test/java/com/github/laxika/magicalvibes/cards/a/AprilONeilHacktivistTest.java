package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AprilONeilHacktivist.class, ChromaticStar.class, GrizzlyBears.class, Shock.class, Ornithopter.class})
class AprilONeilHacktivistTest extends BaseCardTest {

    @Test
    @DisplayName("Draws once for each distinct card type among spells cast this turn")
    void drawsForEachDistinctSpellCardType() {
        harness.addToBattlefield(player1, new AprilONeilHacktivist());
        harness.setHand(player1, List.of(new Shock(), new GrizzlyBears(), new ChromaticStar()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));

        harness.castAndResolveInstant(player1, 0, player2.getId());
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
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        int handBeforeTrigger = gd.playerHands.get(player1.getId()).size();
        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeTrigger + 1);
    }

    @Test
    @DisplayName("Does not draw when no spell was cast this turn")
    void doesNotDrawWithoutSpells() {
        harness.addToBattlefield(player1, new AprilONeilHacktivist());
        harness.setLibrary(player1, List.of(new Shock()));

        int handBeforeTrigger = gd.playerHands.get(player1.getId()).size();
        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeTrigger);
    }

    @Test
    @DisplayName("One artifact creature spell contributes two card types")
    void countsAllTypesOfAMultitypeSpell() {
        harness.addToBattlefield(player1, new AprilONeilHacktivist());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("April counts herself when she was cast this turn")
    void countsHerOwnCreatureSpell() {
        harness.setHand(player1, List.of(new AprilONeilHacktivist()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Spells cast in response to the end step trigger count on resolution")
    void countsSpellsCastInResponse() {
        harness.addToBattlefield(player1, new AprilONeilHacktivist());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Opponent spells do not contribute card types")
    void ignoresOpponentSpells() {
        harness.addToBattlefield(player1, new AprilONeilHacktivist());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLibrary(player1, List.of(new Shock()));

        harness.castAndResolveInstant(player2, 0, player1.getId());
        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("April does not trigger at the opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new AprilONeilHacktivist());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLibrary(player1, List.of(new Shock()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
