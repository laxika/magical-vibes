package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhoxWarMonk.class})
class RhoxWarMonkTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking a player gains controller life equal to combat damage dealt")
    void lifelinkGainsLifeOnAttack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new RhoxWarMonk());
        declareAttackers(List.of(0));

        // Monk deals 3 combat damage: player2 loses 3, player1 gains 3 from lifelink.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Both attacking and blocking monks gain life from damage to creatures")
    void lifelinkGainsLifeForBothControllersInBlockedCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RhoxWarMonk());
        addCreatureReady(player2, new RhoxWarMonk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
        harness.assertOnBattlefield(player1, "Rhox War Monk");
        harness.assertOnBattlefield(player2, "Rhox War Monk");
    }

    @Test
    @DisplayName("Lifelink still gains life when both monks die from combat damage")
    void lifelinkGainsLifeBeforeLethallyDamagedMonksDie() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new RhoxWarMonk());
        Permanent blocker = addCreatureReady(player2, new RhoxWarMonk());
        attacker.addMarkedDamage(null, 1);
        blocker.addMarkedDamage(null, 1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
        harness.assertInGraveyard(player1, "Rhox War Monk");
        harness.assertInGraveyard(player2, "Rhox War Monk");
    }
}
