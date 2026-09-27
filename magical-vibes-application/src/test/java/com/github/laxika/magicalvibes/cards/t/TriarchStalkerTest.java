package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CardUsed({TriarchStalker.class, GrizzlyBears.class})
class TriarchStalkerTest extends BaseCardTest {

    @Test
    void creaturesAttackingChosenPlayerHaveMenace() {
        addCreatureReady(player1, new TriarchStalker());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        assertFalse(gqs.hasKeyword(gd, attacker, Keyword.MENACE));

        beginCombatAndResolveRelay(player1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttacker(player1, attacker));

        assertTrue(gqs.hasKeyword(gd, attacker, Keyword.MENACE));
    }

    @Test
    void creaturesAttackingAnotherPlayerDoNotHaveMenace() {
        addCreatureReady(player1, new TriarchStalker());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        beginCombatAndResolveRelay(player1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttacker(player2, attacker));

        assertFalse(gqs.hasKeyword(gd, attacker, Keyword.MENACE));
    }

    private void beginCombatAndResolveRelay(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }

    private void declareAttacker(Player activePlayer, Permanent attacker) {
        declareAttackers(activePlayer,
                List.of(gd.playerBattlefields.get(activePlayer.getId()).indexOf(attacker)));
    }
}
