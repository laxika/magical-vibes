package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeathgreeterTest extends BaseCardTest {

    // "Whenever another creature dies, you may gain 1 life."

    @Test
    @DisplayName("Accepting the trigger when another creature dies gains 1 life")
    void anotherCreatureDeathAcceptGainsLife() {
        harness.addToBattlefield(player1, new Deathgreeter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock -> bears die -> death trigger queued
        harness.passBothPriorities(); // Resolve the queued may trigger (awaits choice)

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Declining the trigger gains no life")
    void anotherCreatureDeathDeclineGainsNoLife() {
        harness.addToBattlefield(player1, new Deathgreeter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent's creature dying still triggers the may-gain-life ability")
    void opponentCreatureDeathTriggers() {
        harness.addToBattlefield(player1, new Deathgreeter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Deathgreeter dying does not trigger its own ability (another creature only)")
    void ownDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new Deathgreeter());
        harness.setLife(player1, 20);

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID deathgreeterId = harness.getPermanentId(player1, "Deathgreeter");
        harness.castInstant(player2, 0, deathgreeterId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
