package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

@CardUsed({GiantCindermaw.class, AngelOfMercy.class, GoForTheThroat.class, TurnToFrog.class})
class GiantCindermawTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents an opponent from gaining life")
    void preventsOpponentLifeGain() {
        harness.addToBattlefield(player1, new GiantCindermaw());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new AngelOfMercy(), "{4}{W}");
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents its controller from gaining life")
    void preventsControllerLifeGain() {
        harness.addToBattlefield(player1, new GiantCindermaw());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    void allowsLifeGainWhenOnlyInGraveyard() {
        harness.setGraveyard(player1, List.of(new GiantCindermaw()));
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 23);
    }

    @Test
    void allowsLifeGainIfRemovedBeforeTriggerResolves() {
        Permanent cindermaw = harness.addToBattlefieldAndReturn(player1, new GiantCindermaw());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, cindermaw.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Giant Cindermaw");
        harness.assertLife(player1, 23);
    }

    @Test
    void allowsLifeGainAfterLosingAllAbilities() {
        Permanent cindermaw = harness.addToBattlefieldAndReturn(player1, new GiantCindermaw());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, cindermaw.getId());

        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Giant Cindermaw");
        harness.assertLife(player1, 23);
    }

    @Test
    void tramplesOverBlockerWhileLifeLossStillWorks() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GiantCindermaw());
        Permanent blocker = addCreatureReady(player2, new AngelOfMercy());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Giant Cindermaw");
        harness.assertInGraveyard(player2, "Angel of Mercy");
    }
}
