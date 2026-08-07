package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrbsOfWardingTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents cannot target the controller with a spell")
    void opponentCannotTargetController() {
        harness.addToBattlefield(player1, new OrbsOfWarding());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Prevents 1 of each attacking creature's combat damage to the controller")
    void preventsOneCombatDamage() {
        harness.addToBattlefield(player1, new OrbsOfWarding());
        harness.setLife(player1, 20);

        Permanent giant = new Permanent(new HillGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.<BlockerAssignment>of());
        harness.passBothPriorities();

        // Hill Giant deals 3; 1 is prevented, so player1 takes 2.
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents 1 of a creature's noncombat damage to the controller")
    void preventsOneNoncombatCreatureDamage() {
        harness.addToBattlefield(player1, new OrbsOfWarding());
        harness.setLife(player1, 20);
        Permanent tim = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        tim.setSummoningSick(false);

        // The controller may target themselves; hexproof only stops opponents.
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tim),
                null, player1.getId());
        harness.passBothPriorities();

        // The single point of damage is prevented entirely.
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent damage from a noncreature source")
    void doesNotPreventNoncreatureDamage() {
        harness.addToBattlefield(player1, new OrbsOfWarding());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Shock is not a creature, so all 2 damage lands.
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }
}
