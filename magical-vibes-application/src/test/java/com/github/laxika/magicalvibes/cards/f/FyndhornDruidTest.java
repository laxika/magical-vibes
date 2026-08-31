package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AgentOfStromgald;
import com.github.laxika.magicalvibes.cards.g.GorillaChieftain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({FyndhornDruid.class, GorillaChieftain.class, AgentOfStromgald.class})
class FyndhornDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Dying after being blocked gains 4 life")
    void diesAfterBeingBlockedGainsLife() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new FyndhornDruid());
        harness.addToBattlefield(player2, new GorillaChieftain()); // 3/3 kills the 2/2 Druid

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Fyndhorn Druid");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Dying later in the turn after being blocked gains 4 life")
    void diesLaterInTurnAfterBeingBlockedGainsLife() {
        harness.setLife(player1, 20);
        Permanent druid = addCreatureReady(player1, new FyndhornDruid());
        addCreatureReady(player2, new AgentOfStromgald());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.assertOnBattlefield(player1, "Fyndhorn Druid");
        druid.setMarkedDamage(2);
        harness.runStateBasedActions();
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
        addCreatureReady(player1, new GorillaChieftain());
        addCreatureReady(player2, new FyndhornDruid());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Fyndhorn Druid");
        harness.assertLife(player2, 20);
    }
}
