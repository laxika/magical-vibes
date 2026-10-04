package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImperialCeratops.class, Shock.class, FugitiveWizard.class})
class ImperialCeratopsTest extends BaseCardTest {

    @Test
    void spellDamageTriggersEnrage() {
        harness.addToBattlefield(player2, new ImperialCeratops());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID ceratopsId = harness.getPermanentId(player2, "Imperial Ceratops");
        harness.castInstant(player1, 0, ceratopsId);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 2);
        harness.assertOnBattlefield(player2, "Imperial Ceratops");
    }

    @Test
    void combatDamageTriggersEnrage() {
        Permanent ceratops = addCreatureReady(player2, new ImperialCeratops());
        Permanent attacker = addCreatureReady(player1, new FugitiveWizard());
        attacker.setAttacking(true);
        ceratops.setBlocking(true);
        ceratops.addBlockingTarget(0);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 2);
        harness.assertOnBattlefield(player2, "Imperial Ceratops");
    }

    @Test
    void separateDamageEventsEachGainLifeIncludingLethalDamage() {
        Permanent ceratops = harness.addToBattlefieldAndReturn(player2, new ImperialCeratops());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        for (int event = 1; event <= 3; event++) {
            harness.castInstant(player1, 0, ceratops.getId());
            harness.passBothPriorities();
            resolveAllTriggers();
            harness.assertLife(player2, lifeBefore + 2 * event);
        }

        harness.assertInGraveyard(player2, "Imperial Ceratops");
        harness.assertNotOnBattlefield(player2, "Imperial Ceratops");
    }

    @Test
    void simultaneousCombatDamageFromTwoBlockersTriggersOnlyOnce() {
        addCreatureReady(player1, new ImperialCeratops());
        Permanent firstBlocker = addCreatureReady(player2, new FugitiveWizard());
        Permanent secondBlocker = addCreatureReady(player2, new FugitiveWizard());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(firstBlocker.getId(), 1, secondBlocker.getId(), 2));
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 2);
        harness.assertOnBattlefield(player1, "Imperial Ceratops");
    }
}
