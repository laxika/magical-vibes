package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShinkaGatekeeper.class, Frostling.class})
class ShinkaGatekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Being dealt damage deals that much damage to its controller")
    void beingDealtDamageDamagesController() {
        Permanent gatekeeper = harness.addToBattlefieldAndReturn(player2, new ShinkaGatekeeper());
        harness.addToBattlefieldAndReturn(player1, new Frostling());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, gatekeeper.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The trigger resolves after lethal damage removes it")
    void triggerResolvesAfterLethalDamageRemovesGatekeeper() {
        Permanent gatekeeper = harness.addToBattlefieldAndReturn(player2, new ShinkaGatekeeper());
        harness.addToBattlefieldAndReturn(player1, new Frostling());
        harness.addToBattlefieldAndReturn(player1, new Frostling());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, gatekeeper.getId());
            resolveAllTriggers();
        }

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(gatekeeper);
    }

    @Test
    @DisplayName("Lethal combat damage is reflected in full to each creature's controller")
    void lethalCombatDamageDamagesBothControllers() {
        addCreatureReady(player1, new ShinkaGatekeeper());
        harness.addToBattlefield(player2, new ShinkaGatekeeper());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player1, "Shinka Gatekeeper");
        harness.assertNotOnBattlefield(player2, "Shinka Gatekeeper");
        harness.assertInGraveyard(player1, "Shinka Gatekeeper");
        harness.assertInGraveyard(player2, "Shinka Gatekeeper");
    }

    @Test
    @DisplayName("Dealing combat damage without receiving damage does not hurt its controller")
    void unblockedAttackDoesNotDamageController() {
        addCreatureReady(player1, new ShinkaGatekeeper());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Shinka Gatekeeper");
    }
}
