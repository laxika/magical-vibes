package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BogBadger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

@CardUsed({MossbeardAncient.class, BogBadger.class})
class MossbeardAncientTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldGivesItsControllerFiveLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new MossbeardAncient()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 10);
    }

    @Test
    void lifeGainWaitsForTheEntryTriggerToResolve() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new MossbeardAncient()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.assertLife(player1, 10);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mossbeard Ancient");
        harness.assertLife(player1, 10);

        harness.passBothPriorities();
        harness.assertLife(player1, 15);
    }

    @Test
    void enteringUnderTheOtherPlayersControlGivesThatPlayerLife() {
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.setHand(player2, List.of(new MossbeardAncient()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 15);
    }

    @Test
    void tramplesOverAThreeToughnessBlocker() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MossbeardAncient());
        Permanent blocker = addCreatureReady(player2, new BogBadger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3, player2.getId(), 4));

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Bog Badger");
        harness.assertOnBattlefield(player1, "Mossbeard Ancient");
    }
}
