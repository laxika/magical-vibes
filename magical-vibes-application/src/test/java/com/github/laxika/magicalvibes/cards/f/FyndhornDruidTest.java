package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class FyndhornDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Dying after being blocked gains 4 life")
    void diesAfterBeingBlockedGainsLife() {
        harness.setLife(player1, 20);
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new FyndhornDruid());
        druid.setSummoningSick(false);
        harness.addToBattlefield(player2, new GiantSpider()); // 2/4 kills the 2/2 Druid

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fyndhorn Druid");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Dying without having been blocked gains no life")
    void diesUnblockedGainsNoLife() {
        harness.setLife(player1, 20);
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new FyndhornDruid());

        druid.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fyndhorn Druid");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Blocking a creature is not being blocked — no life gain")
    void diesAfterBlockingGainsNoLife() {
        harness.setLife(player2, 20);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        attacker.setSummoningSick(false);
        Permanent druid = harness.addToBattlefieldAndReturn(player2, new FyndhornDruid());
        druid.setSummoningSick(false);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Fyndhorn Druid");
        harness.assertLife(player2, 20);
    }
}
