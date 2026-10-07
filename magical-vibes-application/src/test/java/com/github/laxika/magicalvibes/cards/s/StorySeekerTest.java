package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({StorySeeker.class})
class StorySeekerTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldDoesNotGainLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new StorySeeker()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Story Seeker");
        harness.assertLife(player1, 10);
    }

    @Test
    void unblockedCombatDamageGainsLifeForItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player2, new StorySeeker());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }

    @Test
    void attackerAndBlockerGainLifeEvenWhenBothDie() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new StorySeeker());
        addCreatureReady(player2, new StorySeeker());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
        harness.assertNotOnBattlefield(player1, "Story Seeker");
        harness.assertNotOnBattlefield(player2, "Story Seeker");
        harness.assertInGraveyard(player1, "Story Seeker");
        harness.assertInGraveyard(player2, "Story Seeker");
    }
}
