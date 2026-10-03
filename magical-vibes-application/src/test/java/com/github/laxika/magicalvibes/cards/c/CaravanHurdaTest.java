package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CaravanHurda.class})
class CaravanHurdaTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage dealt to a player gains that much life")
    void combatDamageToPlayerGainsLife() {
        Permanent hurda = addCreatureReady(player1, new CaravanHurda());
        hurda.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Both attacking and blocking Hurda gain life from damage to creatures")
    void combatDamageToCreaturesGainsLifeForBothControllers() {
        addCreatureReady(player1, new CaravanHurda());
        addCreatureReady(player2, new CaravanHurda());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
        harness.assertOnBattlefield(player1, "Caravan Hurda");
        harness.assertOnBattlefield(player2, "Caravan Hurda");
    }
}
