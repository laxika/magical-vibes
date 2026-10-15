package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.p.PatrolHound;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Halberdier.class, PatrolHound.class})
class HalberdierTest extends BaseCardTest {

    @Test
    @DisplayName("First strike lets Halberdier destroy a 2/2 blocker before it deals combat damage")
    void firstStrikeDealsDamageBeforeRegularCombatDamage() {
        addCreatureReady(player1, new Halberdier());
        addCreatureReady(player2, new PatrolHound());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.assertOnBattlefield(player1, "Halberdier");
        harness.assertInGraveyard(player2, "Patrol Hound");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("First strike lets a blocking Halberdier destroy the attacker before taking damage")
    void firstStrikeWorksWhileBlocking() {
        addCreatureReady(player1, new PatrolHound());
        addCreatureReady(player2, new Halberdier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.assertInGraveyard(player1, "Patrol Hound");
        harness.assertOnBattlefield(player2, "Halberdier");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opposing Halberdiers deal first strike damage simultaneously and both die")
    void opposingFirstStrikersDealDamageSimultaneously() {
        addCreatureReady(player1, new Halberdier());
        addCreatureReady(player2, new Halberdier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        harness.assertInGraveyard(player1, "Halberdier");
        harness.assertInGraveyard(player2, "Halberdier");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An unblocked Halberdier deals damage only once during combat")
    void unblockedFirstStrikerDoesNotDealRegularCombatDamage() {
        addCreatureReady(player1, new Halberdier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        resolveCombat();

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Halberdier");
    }
}
