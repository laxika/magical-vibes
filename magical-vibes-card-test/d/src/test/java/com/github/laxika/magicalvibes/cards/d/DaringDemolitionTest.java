package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AegisAutomaton;
import com.github.laxika.magicalvibes.cards.c.ConsulateDreadnought;
import com.github.laxika.magicalvibes.cards.u.UniversalSolvent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaringDemolition.class, AegisAutomaton.class, ConsulateDreadnought.class, UniversalSolvent.class})
class DaringDemolitionTest extends BaseCardTest {

    @Test
    @DisplayName("Daring Demolition destroys a target creature")
    void destroysCreature() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());

        castDaringDemolition(automaton);

        harness.assertNotOnBattlefield(player2, "Aegis Automaton");
        harness.assertInGraveyard(player2, "Aegis Automaton");
    }

    @Test
    @DisplayName("Daring Demolition destroys a noncreature Vehicle")
    void destroysVehicle() {
        Permanent dreadnought = harness.addToBattlefieldAndReturn(player2, new ConsulateDreadnought());

        castDaringDemolition(dreadnought);

        harness.assertNotOnBattlefield(player2, "Consulate Dreadnought");
        harness.assertInGraveyard(player2, "Consulate Dreadnought");
    }

    @Test
    @DisplayName("Daring Demolition cannot target a noncreature non-Vehicle permanent")
    void cannotTargetOtherPermanent() {
        Permanent solvent = harness.addToBattlefieldAndReturn(player2, new UniversalSolvent());
        harness.setHand(player1, List.of(new DaringDemolition()));
        addDaringDemolitionMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, solvent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    @Test
    @DisplayName("Daring Demolition can destroy its controller's creature")
    void destroysOwnCreature() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new AegisAutomaton());

        castDaringDemolition(automaton);

        harness.assertNotOnBattlefield(player1, "Aegis Automaton");
        harness.assertInGraveyard(player1, "Aegis Automaton");
        harness.assertInGraveyard(player1, "Daring Demolition");
    }

    @Test
    @DisplayName("Daring Demolition destroys only its chosen target")
    void leavesOtherLegalTargetsUntouched() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player2, new AegisAutomaton());
        harness.addToBattlefield(player2, new ConsulateDreadnought());
        harness.addToBattlefield(player1, new AegisAutomaton());

        castDaringDemolition(automaton);

        harness.assertInGraveyard(player2, "Aegis Automaton");
        harness.assertOnBattlefield(player2, "Consulate Dreadnought");
        harness.assertOnBattlefield(player1, "Aegis Automaton");
    }

    private void castDaringDemolition(Permanent target) {
        harness.setHand(player1, List.of(new DaringDemolition()));
        addDaringDemolitionMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addDaringDemolitionMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
