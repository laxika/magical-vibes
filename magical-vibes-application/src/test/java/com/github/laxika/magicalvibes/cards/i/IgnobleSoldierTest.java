package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IgnobleSoldier.class, FreshVolunteers.class})
class IgnobleSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("When blocked, Ignoble Soldier deals no combat damage this turn")
    void blockedSoldierDealsNoCombatDamage() {
        Permanent soldier = addCreatureReady(player1, new IgnobleSoldier());
        soldier.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Fresh Volunteers");
        harness.assertInGraveyard(player1, "Ignoble Soldier");
    }

    @Test
    @DisplayName("When unblocked, Ignoble Soldier deals combat damage normally")
    void unblockedSoldierDealsCombatDamage() {
        Permanent soldier = addCreatureReady(player1, new IgnobleSoldier());
        soldier.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
