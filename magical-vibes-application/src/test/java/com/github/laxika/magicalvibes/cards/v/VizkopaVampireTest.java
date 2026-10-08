package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({VizkopaVampire.class})
class VizkopaVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Lifelink gains life from combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent vampire = addCreatureReady(player1, new VizkopaVampire());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(vampire)));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Both controllers gain full damage as life when attacking and blocking vampires die")
    void lifelinkGainsFullDamageForBothControllersDespiteLethalDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new VizkopaVampire());
        addCreatureReady(player2, new VizkopaVampire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
        harness.assertNotOnBattlefield(player1, "Vizkopa Vampire");
        harness.assertNotOnBattlefield(player2, "Vizkopa Vampire");
        harness.assertInGraveyard(player1, "Vizkopa Vampire");
        harness.assertInGraveyard(player2, "Vizkopa Vampire");
    }
}
