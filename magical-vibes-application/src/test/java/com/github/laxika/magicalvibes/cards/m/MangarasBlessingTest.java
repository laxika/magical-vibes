package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MangarasBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting it gains the controller 5 life")
    void castingGains5Life() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MangarasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
        harness.assertInGraveyard(player1, "Mangara's Blessing");
    }

    @Test
    @DisplayName("Discarded by an opponent's Distress: gains 2 life without prompting for a target")
    void discardedByOpponentGains2Life() {
        harness.setHand(player2, new ArrayList<>(List.of(new MangarasBlessing())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player1 chooses Mangara's Blessing from player2's revealed hand
        harness.handleCardChosen(player1, 0);

        // The trigger is non-targeting — it goes straight on the stack, no permanent choice
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities(); // resolve the discard trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        harness.assertInGraveyard(player2, "Mangara's Blessing");
    }

    @Test
    @DisplayName("Returns from the graveyard to hand at the beginning of the next end step")
    void returnsToHandAtNextEndStep() {
        harness.setHand(player2, new ArrayList<>(List.of(new MangarasBlessing())));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities(); // resolve the discard trigger

        harness.assertInGraveyard(player2, "Mangara's Blessing");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // -> END_STEP

        harness.assertNotInGraveyard(player2, "Mangara's Blessing");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Mangara's Blessing");
    }

    @Test
    @DisplayName("Does not trigger when its own controller discards it")
    void doesNotTriggerOnSelfDiscard() {
        harness.setLife(player1, 20);

        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new Sift(), new MangarasBlessing()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // Sift draws 3, prompts for discard

        harness.handleCardChosen(player1, 0); // discard Mangara's Blessing

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Mangara's Blessing");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // -> END_STEP

        // No delayed return was registered
        harness.assertInGraveyard(player1, "Mangara's Blessing");
    }
}
