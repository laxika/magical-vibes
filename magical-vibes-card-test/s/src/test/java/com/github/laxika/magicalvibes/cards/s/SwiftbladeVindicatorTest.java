package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwiftbladeVindicator.class, LlanowarElves.class})
class SwiftbladeVindicatorTest extends BaseCardTest {

    @Test
    void attackingDoesNotTapAndDealsDamageTwice() {
        harness.setLife(player2, 20);
        Permanent vindicator = addCreatureReady(player1, new SwiftbladeVindicator());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(vindicator.isTapped()).isFalse();
        harness.assertLife(player2, 18);
    }

    @Test
    void tramplesInRegularDamageAfterKillingBlockerInFirstStrikeDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SwiftbladeVindicator());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 1));

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Swiftblade Vindicator");
        harness.assertLife(player2, 19);
    }

    @Test
    void blockingKillsAttackerBeforeItDealsRegularCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new SwiftbladeVindicator());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Swiftblade Vindicator");
        harness.assertLife(player2, 20);
    }
}
