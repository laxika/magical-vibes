package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrecognitionField;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({JemLightfooteSkyExplorer.class, GrizzlyBears.class, PrecognitionField.class, Shock.class})
class JemLightfooteSkyExplorerTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card at the end step when no spell was cast from hand")
    void drawsAtEndStepWhenNoSpellWasCastFromHand() {
        addJem();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Shock()));

        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not draw a card after a spell was cast from hand")
    void doesNotDrawAfterHandSpell() {
        addJem();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws a card when the only spell cast was from the library")
    void drawsAfterLibrarySpell() {
        addJem();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new PrecognitionField());
        harness.setLibrary(player1, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromLibraryTop(player1, player2.getId());
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private Permanent addJem() {
        return harness.addToBattlefieldAndReturn(player1, new JemLightfooteSkyExplorer());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
