package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({DaybreakChaplain.class})
class DaybreakChaplainTest extends BaseCardTest {

    @Test
    void unblockedDamageGainsLifeForController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DaybreakChaplain());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void attackingAndBlockingChaplainBothGainLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DaybreakChaplain());
        harness.addToBattlefield(player2, new DaybreakChaplain());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
        harness.assertOnBattlefield(player1, "Daybreak Chaplain");
        harness.assertOnBattlefield(player2, "Daybreak Chaplain");
    }

    @Test
    void lifelinkStillGainsLifeWhenBothCreaturesDie() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DaybreakChaplain()).setMarkedDamage(2);
        harness.addToBattlefield(player2, new DaybreakChaplain());
        findPermanent(player2, "Daybreak Chaplain").setMarkedDamage(2);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
        harness.assertNotOnBattlefield(player1, "Daybreak Chaplain");
        harness.assertNotOnBattlefield(player2, "Daybreak Chaplain");
        harness.assertInGraveyard(player1, "Daybreak Chaplain");
        harness.assertInGraveyard(player2, "Daybreak Chaplain");
    }

    @Test
    void preventedDamageDoesNotGainLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DaybreakChaplain());
        harness.addToBattlefield(player2, new DaybreakChaplain());
        findPermanent(player2, "Daybreak Chaplain").setDamagePreventionShield(1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
    }
}

