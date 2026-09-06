package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({BatteringRam.class, WallOfWood.class})
class BatteringRamTest extends BaseCardTest {

    @Test
    @DisplayName("Battering Ram gains banding for the current combat only")
    void bandingExpiresAtEndOfCombat() {
        Permanent ram = addCreatureReady(player1, new BatteringRam());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ram, Keyword.BANDING)).isTrue();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ram, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("When Battering Ram becomes blocked by a Wall, that Wall is scheduled for end-of-combat destruction")
    void becomesBlockedByWallSchedulesDestruction() {
        Permanent ram = addCreatureReady(player1, new BatteringRam());
        ram.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfWood());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Battering Ram")
                        && se.getTargetId().equals(wall.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(wall.getId()));
    }

    @Test
    @DisplayName("A blocking Wall is destroyed at end of combat")
    void blockingWallDestroyedAtEndOfCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent ram = addCreatureReady(player1, new BatteringRam());
        ram.setAttacking(true);
        addCreatureReady(player2, new WallOfWood()); // 0/3 survives Battering Ram's 1 damage

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Wood");
        harness.assertInGraveyard(player2, "Wall of Wood");
    }

    @Test
    @DisplayName("When Battering Ram becomes blocked by a non-Wall creature, nothing is scheduled for destruction")
    @CardUsed(GiantSpider.class)
    void becomesBlockedByNonWallSchedulesNothing() {
        Permanent ram = addCreatureReady(player1, new BatteringRam());
        ram.setAttacking(true);
        addCreatureReady(player2, new GiantSpider()); // not a Wall

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }
}
