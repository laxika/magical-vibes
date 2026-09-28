package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({VoraciousCobra.class, AncientKavu.class})
class VoraciousCobraTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a creature destroys that creature")
    void combatDamageToCreatureDestroysIt() {
        Permanent cobra = addCreatureReady(player1, new VoraciousCobra());
        cobra.setAttacking(true);
        addCreatureReady(player2, new AncientKavu());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Ancient Kavu");
        harness.assertInGraveyard(player2, "Ancient Kavu");
        harness.assertOnBattlefield(player1, "Voracious Cobra");
    }

    @Test
    @DisplayName("Combat damage to a player does not destroy a creature")
    void combatDamageToPlayerDoesNotTrigger() {
        Permanent cobra = addCreatureReady(player1, new VoraciousCobra());
        cobra.setAttacking(true);
        addCreatureReady(player2, new AncientKavu());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Ancient Kavu");
    }
}
