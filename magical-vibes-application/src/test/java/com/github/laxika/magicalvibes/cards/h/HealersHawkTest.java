package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HealersHawk.class, SavannahLions.class})
class HealersHawkTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a creature without flying or reach from blocking")
    void cannotBeBlockedByGroundCreature() {
        addCreatureReady(player1, new HealersHawk());
        addCreatureReady(player2, new SavannahLions());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains life equal to combat damage dealt")
    void gainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HealersHawk());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Flying blockers gain life from damage even when both Hawks die")
    void bothHawksGainLifeFromLethalCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HealersHawk());
        addCreatureReady(player2, new HealersHawk());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
        harness.assertNotOnBattlefield(player1, "Healer's Hawk");
        harness.assertNotOnBattlefield(player2, "Healer's Hawk");
        harness.assertInGraveyard(player1, "Healer's Hawk");
        harness.assertInGraveyard(player2, "Healer's Hawk");
    }

    @Test
    @DisplayName("Hawk can block a ground attacker and gains life despite dying")
    void gainsLifeWhenBlockingGroundAttacker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SavannahLions());
        addCreatureReady(player2, new HealersHawk());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
        harness.assertInGraveyard(player1, "Savannah Lions");
        harness.assertInGraveyard(player2, "Healer's Hawk");
    }
}
