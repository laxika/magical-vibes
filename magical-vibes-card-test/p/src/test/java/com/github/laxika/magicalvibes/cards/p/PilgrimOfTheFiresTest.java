package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PilgrimOfTheFires.class, AirElemental.class})
class PilgrimOfTheFiresTest extends BaseCardTest {

    @Test
    @DisplayName("First strike lets it survive combat while trample damages the defending player")
    void firstStrikeAndTrampleApplyInCombat() {
        harness.setLife(player2, 20);
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfTheFires());
        Permanent blocker = addCreatureReady(player2, new AirElemental());

        pilgrim.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class))
                .isNotNull();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 4,
                player2.getId(), 2
        ));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pilgrim);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("An unblocked Pilgrim deals damage only once, despite first strike")
    void unblockedFirstStrikerDealsDamageOnlyOnce() {
        harness.setLife(player2, 20);
        Permanent pilgrim = addCreatureReady(player1, new PilgrimOfTheFires());
        pilgrim.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pilgrim);
    }

    @Test
    @DisplayName("First striking combatants deal damage simultaneously and trample still applies")
    void opposingFirstStrikerTradesWhileTrampleDealsExcessDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new PilgrimOfTheFires());
        Permanent blocker = addCreatureReady(player2, new PilgrimOfTheFires());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 4,
                player2.getId(), 2
        ));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Trample requires lethal damage to the blocker before assigning damage to the player")
    void cannotAssignExcessDamageBeforeLethalDamageToBlocker() {
        Permanent attacker = addCreatureReady(player1, new PilgrimOfTheFires());
        Permanent blocker = addCreatureReady(player2, new PilgrimOfTheFires());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 3
        ))).isInstanceOf(IllegalStateException.class);
    }
}
