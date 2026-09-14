package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SaprazzanRaider.class, FreshVolunteers.class})
class SaprazzanRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to its owner's hand when it becomes blocked")
    void returnsToHandWhenBlocked() {
        addCreatureReady(player1, new SaprazzanRaider());
        addCreatureReady(player2, new FreshVolunteers());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertInHand(player1, "Saprazzan Raider");
        harness.assertNotOnBattlefield(player1, "Saprazzan Raider");
    }

    @Test
    @DisplayName("Stays on the battlefield when it is not blocked")
    void staysOnBattlefieldWhenNotBlocked() {
        addCreatureReady(player1, new SaprazzanRaider());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Saprazzan Raider");
        harness.assertNotInHand(player1, "Saprazzan Raider");
    }

    @Test
    @DisplayName("Returns to its owner's hand when controlled by another player and blocked")
    void returnsToOwnersHandWhenControlledByAnotherPlayer() {
        SaprazzanRaider card = new SaprazzanRaider();
        card.setOwnerId(player2.getId());
        Permanent raider = addCreatureReady(player1, card);
        raider.setAttacking(true);
        addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertInHand(player2, "Saprazzan Raider");
        harness.assertNotInHand(player1, "Saprazzan Raider");
        harness.assertNotOnBattlefield(player1, "Saprazzan Raider");
    }
}
