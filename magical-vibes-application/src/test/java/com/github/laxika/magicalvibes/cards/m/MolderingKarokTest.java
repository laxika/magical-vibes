package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MolderingKarok.class, GrizzlyBears.class})
class MolderingKarokTest extends BaseCardTest {

    @Test
    @DisplayName("Lifelink gains life equal to combat damage dealt")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent karok = addCreatureReady(player1, new MolderingKarok());
        karok.setAttacking(true);
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent karok = addCreatureReady(player1, new MolderingKarok());
        karok.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1));

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Both attacking and blocking Karoks gain life before dying to simultaneous damage")
    void lifelinkAppliesToBothCreaturesThatDieInCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new MolderingKarok());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new MolderingKarok());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
        harness.assertNotOnBattlefield(player1, "Moldering Karok");
        harness.assertNotOnBattlefield(player2, "Moldering Karok");
        harness.assertInGraveyard(player1, "Moldering Karok");
        harness.assertInGraveyard(player2, "Moldering Karok");
    }

    @Test
    @DisplayName("Trample allows all damage to be assigned to the blocker and lifelink counts overkill")
    void mayAssignAllDamageToBlocker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent karok = addCreatureReady(player1, new MolderingKarok());
        karok.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Moldering Karok");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Trample requires lethal damage to the blocker before assigning damage to the player")
    void rejectsOverflowBeforeLethalDamageIsAssigned() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent karok = addCreatureReady(player1, new MolderingKarok());
        karok.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 1));

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
