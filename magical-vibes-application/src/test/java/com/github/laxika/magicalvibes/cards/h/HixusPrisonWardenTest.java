package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class HixusPrisonWardenTest extends BaseCardTest {

    /** Puts Hixus onto player2's battlefield, optionally recorded as having entered this turn. */
    private Permanent putHixus(boolean enteredThisTurn) {
        Permanent hixus = harness.addToBattlefieldAndReturn(player2, new HixusPrisonWarden());
        hixus.setSummoningSick(false);
        if (enteredThisTurn) {
            gd.permanentsEnteredBattlefieldThisTurn
                    .computeIfAbsent(player2.getId(), k -> new ArrayList<>())
                    .add(hixus.getCard());
        }
        return hixus;
    }

    /** Attacks player2 with a Grizzly Bears and lets combat damage resolve. */
    private void attackWithBears() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage
        harness.passBothPriorities(); // exile trigger resolves
    }

    @Test
    @DisplayName("Creature dealing combat damage is exiled when Hixus entered this turn")
    void exilesCombatDamageSourceWhenEnteredThisTurn() {
        putHixus(true);
        attackWithBears();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Nothing is exiled when Hixus did not enter this turn")
    void doesNotExileWhenHixusEnteredEarlier() {
        putHixus(false);
        attackWithBears();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiled creature returns when Hixus leaves the battlefield")
    void exiledCreatureReturnsWhenHixusLeaves() {
        Permanent hixus = putHixus(true);
        attackWithBears();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        hixus.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hixus, Prison Warden");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
