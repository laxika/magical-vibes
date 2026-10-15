package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.j.JeskaiWindscout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GurmagSwiftwing.class, AlpineGrizzly.class, JeskaiWindscout.class})
class GurmagSwiftwingTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GurmagSwiftwing());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new AlpineGrizzly());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First strike defeats a one-toughness flying blocker before it deals damage")
    void firstStrikeDefeatsSmallerBlocker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GurmagSwiftwing());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new JeskaiWindscout());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gurmag Swiftwing");
        harness.assertInGraveyard(player2, "Jeskai Windscout");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Haste allows Gurmag Swiftwing to attack immediately after entering")
    void hasteAllowsImmediateAttack() {
        harness.setHand(player1, List.of(new GurmagSwiftwing()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Gurmag Swiftwing can block a flyer and kill it before regular damage")
    void flyingAndFirstStrikeWorkWhileBlocking() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new JeskaiWindscout());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new GurmagSwiftwing());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jeskai Windscout");
        harness.assertOnBattlefield(player2, "Gurmag Swiftwing");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opposing Swiftwings both deal first-strike damage and survive")
    void opposingFirstStrikersDealDamageSimultaneously() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GurmagSwiftwing());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GurmagSwiftwing());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            harness.passUntil(TurnStep.END_OF_COMBAT);
        });

        harness.assertOnBattlefield(player1, "Gurmag Swiftwing");
        harness.assertOnBattlefield(player2, "Gurmag Swiftwing");
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 20);
    }
}
