package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.n.NecronDeathmark;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TriarchStalker.class, NecronDeathmark.class})
class TriarchStalkerTest extends BaseCardTest {

    @Test
    void creaturesAttackingChosenPlayerHaveMenace() {
        addCreatureReady(player1, new TriarchStalker());
        Permanent attacker = addCreatureReady(player1, new NecronDeathmark());

        assertFalse(gqs.hasKeyword(gd, attacker, Keyword.MENACE));

        beginCombatAndResolveRelay(player1);
        declareAttackersAndPrepareBlockers(player1, List.of(1));

        assertTrue(gqs.hasKeyword(gd, attacker, Keyword.MENACE));
    }

    @Test
    void creaturesAttackingAnotherPlayerDoNotHaveMenace() {
        addCreatureReady(player1, new TriarchStalker());
        Permanent attacker = addCreatureReady(player2, new NecronDeathmark());

        beginCombatAndResolveRelay(player1);
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertFalse(gqs.hasKeyword(gd, attacker, Keyword.MENACE));
    }

    @Test
    void stalkerItselfHasMenaceWhileAttackingChosenPlayer() {
        Permanent stalker = addCreatureReady(player1, new TriarchStalker());

        beginCombatAndResolveRelay(player1);
        assertFalse(gqs.hasKeyword(gd, stalker, Keyword.MENACE));
        declareAttackersAndPrepareBlockers(List.of(0));

        assertTrue(gqs.hasKeyword(gd, stalker, Keyword.MENACE));
    }

    @Test
    void attackingBeforeAnyOpponentWasChosenDoesNotGrantMenace() {
        addCreatureReady(player1, new TriarchStalker());
        Permanent attacker = addCreatureReady(player1, new NecronDeathmark());

        declareAttackersAndPrepareBlockers(List.of(1));

        assertFalse(gqs.hasKeyword(gd, attacker, Keyword.MENACE));
    }

    @Test
    void opponentsBeginningOfCombatDoesNotChooseAPlayer() {
        addCreatureReady(player1, new TriarchStalker());
        Permanent attacker = addCreatureReady(player1, new NecronDeathmark());

        beginCombatAndResolveRelay(player2);
        declareAttackersAndPrepareBlockers(player1, List.of(1));

        assertFalse(gqs.hasKeyword(gd, attacker, Keyword.MENACE));
    }

    @Test
    void menaceEndsWhenCreatureStopsAttacking() {
        addCreatureReady(player1, new TriarchStalker());
        Permanent attacker = addCreatureReady(player1, new NecronDeathmark());

        beginCombatAndResolveRelay(player1);
        declareAttackersAndPrepareBlockers(List.of(1));
        assertTrue(gqs.hasKeyword(gd, attacker, Keyword.MENACE));

        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertFalse(gqs.hasKeyword(gd, attacker, Keyword.MENACE));
    }

    @Test
    void nonattackingCreaturesDoNotGainMenaceAfterChoice() {
        Permanent stalker = addCreatureReady(player1, new TriarchStalker());
        Permanent attacker = addCreatureReady(player1, new NecronDeathmark());
        Permanent stationary = addCreatureReady(player1, new NecronDeathmark());
        Permanent opposingCreature = addCreatureReady(player2, new NecronDeathmark());

        beginCombatAndResolveRelay(player1);
        declareAttackersAndPrepareBlockers(List.of(1));

        assertTrue(gqs.hasKeyword(gd, attacker, Keyword.MENACE));
        assertFalse(gqs.hasKeyword(gd, stalker, Keyword.MENACE));
        assertFalse(gqs.hasKeyword(gd, stationary, Keyword.MENACE));
        assertFalse(gqs.hasKeyword(gd, opposingCreature, Keyword.MENACE));
    }

    @Test
    void menaceEndsWhenStalkerLeavesBattlefield() {
        Permanent stalker = addCreatureReady(player1, new TriarchStalker());
        Permanent attacker = addCreatureReady(player1, new NecronDeathmark());

        beginCombatAndResolveRelay(player1);
        declareAttackersAndPrepareBlockers(List.of(1));
        assertTrue(gqs.hasKeyword(gd, attacker, Keyword.MENACE));

        gd.playerBattlefields.get(player1.getId()).remove(stalker);

        assertFalse(gqs.hasKeyword(gd, attacker, Keyword.MENACE));
    }

    @Test
    void grantedMenaceRequiresTwoBlockers() {
        addCreatureReady(player1, new TriarchStalker());
        addCreatureReady(player1, new NecronDeathmark());
        Permanent firstBlocker = addCreatureReady(player2, new NecronDeathmark());
        Permanent secondBlocker = addCreatureReady(player2, new NecronDeathmark());

        beginCombatAndResolveRelay(player1);
        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1), new BlockerAssignment(1, 1))));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    private void beginCombatAndResolveRelay(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }
}
