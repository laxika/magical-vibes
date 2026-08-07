package com.github.laxika.magicalvibes.cards.g;

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

class GuardianAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Guardian Automaton dies from Wrath of God, controller gains 3 life")
    void diesGainsThreeLife() {
        harness.addToBattlefield(player1, new GuardianAutomaton());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.getLife(player1.getId());

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Guardian Automaton");

        // Resolve the death trigger from the stack
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Guardian Automaton survives combat, no life gained")
    void survivesNoLifeGain() {
        GuardianAutomaton automaton = new GuardianAutomaton();
        Permanent blocker = new Permanent(automaton);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(blocker);

        GrizzlyBears weakAttacker = new GrizzlyBears();
        weakAttacker.setPower(0);
        weakAttacker.setToughness(2);
        Permanent attacker = new Permanent(weakAttacker);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        GameData gd = harness.getGameData();
        int lifeBefore = gd.getLife(player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Guardian Automaton");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
