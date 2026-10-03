package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfVerbosity.class, Forest.class, GrizzlyBears.class, JaceBeleren.class})
class CurseOfVerbosityTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted player is attacked, the controller and attacking player draw")
    void controllerAndAttackingPlayerDraw() {
        placeCurseOnPlayer1();
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Attacking the enchanted player's planeswalker does not trigger")
    void attackingPlaneswalkerDoesNotTrigger() {
        placeCurseOnPlayer1();
        addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller draws only once when attacking the enchanted opponent")
    void controllerAttackingEnchantedOpponentDrawsOnce() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfVerbosity());
        curse.setAttachedTo(player2.getId());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Multiple attackers give each eligible player only one draw")
    void multipleAttackersDrawOnlyOnce() {
        placeCurseOnPlayer1();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Attacking an unenchanted player gives neither player a draw")
    void attackingUnenchantedPlayerDoesNotTrigger() {
        placeCurseOnPlayer1();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the controller draws if all attackers die before resolution")
    void attackingOpponentDoesNotDrawAfterAllAttackersDie() {
        placeCurseOnPlayer1();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        attacker.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void placeCurseOnPlayer1() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfVerbosity());
        curse.setAttachedTo(player1.getId());
    }
}
