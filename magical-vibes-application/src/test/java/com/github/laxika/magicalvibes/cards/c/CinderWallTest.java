package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CinderWall.class, BenalishInfantry.class})
class CinderWallTest extends BaseCardTest {

    @Test
    @DisplayName("When Cinder Wall blocks, it schedules itself for end-of-combat destruction")
    void blockingSchedulesSelfDestruction() {
        Permanent attacker = addCreatureReady(player1, new BenalishInfantry());
        attacker.setAttacking(true);
        Permanent cinderWall = addCreatureReady(player2, new CinderWall());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // A block trigger from Cinder Wall fires (non-targeting, references itself)
        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Cinder Wall"));

        // Resolving it schedules Cinder Wall itself for destruction at end of combat
        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(cinderWall.getId()));
        harness.assertOnBattlefield(player2, "Cinder Wall");
    }

    @Test
    @DisplayName("Cinder Wall survives combat damage but is destroyed at end of combat")
    void survivesCombatDamageButDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new BenalishInfantry());
        attacker.setAttacking(true);
        addCreatureReady(player2, new CinderWall());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the trigger, then advance through end of combat
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player2, "Cinder Wall");
        harness.assertInGraveyard(player2, "Cinder Wall");
    }

    @Test
    @DisplayName("Cinder Wall that never blocks is not scheduled for destruction")
    void notDestroyedWhenItDoesNotBlock() {
        Permanent attacker = addCreatureReady(player1, new BenalishInfantry());
        attacker.setAttacking(true);
        addCreatureReady(player2, new CinderWall());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of()); // Cinder Wall stays back

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }
}
