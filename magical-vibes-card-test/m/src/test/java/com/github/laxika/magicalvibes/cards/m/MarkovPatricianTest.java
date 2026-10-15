package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({MarkovPatrician.class, Moonmist.class})
class MarkovPatricianTest extends BaseCardTest {

    @Test
    void unblockedDamageGainsLifeForController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MarkovPatrician());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void lethalCombatStillGainsFullDamageForBothControllers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MarkovPatrician());
        harness.addToBattlefield(player2, new MarkovPatrician());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
        harness.assertNotOnBattlefield(player1, "Markov Patrician");
        harness.assertNotOnBattlefield(player2, "Markov Patrician");
        harness.assertInGraveyard(player1, "Markov Patrician");
        harness.assertInGraveyard(player2, "Markov Patrician");
    }

    @Test
    void preventedDamageDoesNotGainLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MarkovPatrician());
        harness.setHand(player2, List.of(new Moonmist()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        declareAttackers(List.of(0));
        harness.castAndResolveInstant(player2, 0);
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Markov Patrician");
        harness.assertInGraveyard(player2, "Moonmist");
    }
}
