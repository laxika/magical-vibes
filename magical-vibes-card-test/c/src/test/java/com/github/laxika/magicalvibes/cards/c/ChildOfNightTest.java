package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ChildOfNight.class, Fog.class})
class ChildOfNightTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked combat damage gains that much life for the controller")
    void gainsLifeFromUnblockedDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ChildOfNight());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Both controllers gain full damage as life when their creatures trade")
    void gainsLifeEvenWhenDamageIsLethalToBothCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ChildOfNight());
        harness.addToBattlefield(player2, new ChildOfNight());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
        harness.assertNotOnBattlefield(player1, "Child of Night");
        harness.assertNotOnBattlefield(player2, "Child of Night");
        harness.assertInGraveyard(player1, "Child of Night");
        harness.assertInGraveyard(player2, "Child of Night");
    }

    @Test
    @DisplayName("Prevented combat damage does not gain life")
    void preventedDamageDoesNotGainLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ChildOfNight());
        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        declareAttackers(List.of(0));
        harness.castAndResolveInstant(player2, 0);
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Child of Night");
        harness.assertInGraveyard(player2, "Fog");
    }
}
