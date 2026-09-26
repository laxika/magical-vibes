package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CrookshankKobolds;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.w.WallOfDust;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

@CardUsed({InfernalMedusa.class, DurkwoodBoars.class, CrookshankKobolds.class, WallOfDust.class})
class InfernalMedusaTest extends BaseCardTest {

    @Test
    @DisplayName("A non-Wall blocker is destroyed at end of combat")
    void nonWallBlockerDestroyedAtEndOfCombat() {
        addCreatureReady(player1, new InfernalMedusa());
        addCreatureReady(player2, new DurkwoodBoars());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Durkwood Boars");
        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Durkwood Boars");
        harness.assertInGraveyard(player2, "Durkwood Boars");
    }

    @Test
    @DisplayName("A Wall blocker is not destroyed")
    void wallBlockerSurvives() {
        addCreatureReady(player1, new InfernalMedusa());
        addCreatureReady(player2, new WallOfDust());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        resolveCombat();

        harness.assertOnBattlefield(player2, "Wall of Dust");
        harness.assertNotInGraveyard(player2, "Wall of Dust");
    }

    @Test
    @DisplayName("A non-Wall attacker blocked by Infernal Medusa is destroyed at end of combat")
    void blockedNonWallAttackerDestroyedAtEndOfCombat() {
        addCreatureReady(player1, new CrookshankKobolds());
        addCreatureReady(player2, new InfernalMedusa());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Crookshank Kobolds");
        harness.assertInGraveyard(player1, "Crookshank Kobolds");
    }

    @Test
    @DisplayName("A Wall is destroyed when Infernal Medusa blocks it")
    void wallBlockedByMedusaIsDestroyedAtEndOfCombat() {
        Permanent wall = addCreatureReady(player1, new WallOfDust());
        wall.setAttacking(true);
        addCreatureReady(player2, new InfernalMedusa());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Wall of Dust");
        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Wall of Dust");
        harness.assertInGraveyard(player1, "Wall of Dust");
    }

    @Test
    @DisplayName("Each non-Wall blocker is destroyed when Infernal Medusa becomes blocked")
    void eachNonWallBlockerIsDestroyedAtEndOfCombat() {
        addCreatureReady(player1, new InfernalMedusa());
        Permanent firstBlocker = addCreatureReady(player2, new DurkwoodBoars());
        Permanent secondBlocker = addCreatureReady(player2, new DurkwoodBoars());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        resolveAllTriggers();
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 1));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player2, "Durkwood Boars");
        harness.assertInGraveyard(player2, "Durkwood Boars");
    }
}
