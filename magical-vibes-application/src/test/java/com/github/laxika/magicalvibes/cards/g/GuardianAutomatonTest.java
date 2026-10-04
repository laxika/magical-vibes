package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.m.MaritimeGuard;
import com.github.laxika.magicalvibes.cards.r.ReaveSoul;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardianAutomaton.class, WrathOfGod.class, MaritimeGuard.class, ReaveSoul.class, Disperse.class})
class GuardianAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Guardian Automaton dies from Wrath of God, controller gains 3 life")
    void diesGainsThreeLife() {
        harness.addToBattlefield(player1, new GuardianAutomaton());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Guardian Automaton");

        // Resolve the death trigger from the stack
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Guardian Automaton survives combat, no life gained")
    void survivesNoLifeGain() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new GuardianAutomaton());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new MaritimeGuard());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.getLife(player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Guardian Automaton");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Destroying an opponent's Guardian Automaton gains life for its controller after resolution")
    void opposingControllerGainsLifeOnlyWhenTriggerResolves() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player2, new GuardianAutomaton());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new ReaveSoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, automaton.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Guardian Automaton");
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
        assertThat(harness.getGameData().stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 13);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Returning Guardian Automaton to hand does not trigger life gain")
    void returningToHandDoesNotGainLife() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player1, new GuardianAutomaton());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, automaton.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Guardian Automaton");
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
