package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JenovaAncientCalamity.class, GrizzlyBears.class, DoomBlade.class})
class JenovaAncientCalamityTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, another creature gets Jenova's power in counters and becomes a Mutant")
    void buffsAnotherCreatureAndMakesItMutant() {
        addCreatureReady(player1, new JenovaAncientCalamity());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getGrantedSubtypes()).contains(CardSubtype.MUTANT);
    }

    @Test
    @DisplayName("During its controller's turn, Jenova draws cards equal to a dying Mutant's power")
    void drawsDyingMutantsPowerDuringControllerTurn() {
        addCreatureReady(player1, new JenovaAncientCalamity());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.getGrantedSubtypes().add(CardSubtype.MUTANT);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Jenova does not draw when a Mutant dies during an opponent's turn")
    void doesNotDrawDuringOpponentTurn() {
        addCreatureReady(player1, new JenovaAncientCalamity());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.getGrantedSubtypes().add(CardSubtype.MUTANT);

        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
