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

    @Test
    @DisplayName("Blocking Cobra destroys the attacker before regular combat damage")
    void blockingCobraDestroysAttacker() {
        Permanent attacker = addCreatureReady(player1, new AncientKavu());
        attacker.setAttacking(true);
        addCreatureReady(player2, new VoraciousCobra());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ancient Kavu");
        harness.assertNotOnBattlefield(player1, "Ancient Kavu");
        harness.assertOnBattlefield(player2, "Voracious Cobra");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cobra with zero power does not destroy its blocker")
    void zeroPowerDoesNotTriggerDestruction() {
        Permanent cobra = addCreatureReady(player1, new VoraciousCobra());
        cobra.setPowerModifier(-2);
        cobra.setAttacking(true);
        addCreatureReady(player2, new AncientKavu());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Ancient Kavu");
        harness.assertInGraveyard(player1, "Voracious Cobra");
        harness.assertNotOnBattlefield(player1, "Voracious Cobra");
    }
}
