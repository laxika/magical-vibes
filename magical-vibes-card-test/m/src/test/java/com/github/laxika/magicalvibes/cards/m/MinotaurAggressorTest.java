package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RubblebackRhino;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({MinotaurAggressor.class, RubblebackRhino.class})
class MinotaurAggressorTest extends BaseCardTest {

    @Test
    void canAttackTheTurnItIsCastAndDealsDamageOnlyOnce() {
        harness.setHand(player1, List.of(new MinotaurAggressor()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
        harness.assertOnBattlefield(player1, "Minotaur Aggressor");
    }

    @Test
    void killsBlockerBeforeItCanDealDamageWithoutTrampling() {
        harness.addToBattlefield(player1, new MinotaurAggressor());
        addCreatureReady(player2, new RubblebackRhino());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Minotaur Aggressor");
        harness.assertInGraveyard(player2, "Rubbleback Rhino");
        harness.assertLife(player2, 20);
    }

    @Test
    void killsAttackerBeforeItCanDealDamageWhenBlocking() {
        addCreatureReady(player1, new RubblebackRhino());
        harness.addToBattlefield(player2, new MinotaurAggressor());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Minotaur Aggressor");
        harness.assertInGraveyard(player1, "Rubbleback Rhino");
        harness.assertLife(player2, 20);
    }
}
