package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.o.OreplatePangolin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hullcarver.class, OreplatePangolin.class})
class HullcarverTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch kills a larger creature it damages in combat")
    void deathtouchKillsLargerCreature() {
        Permanent hullcarver = addCreatureReady(player1, new Hullcarver());
        Permanent blocker = addCreatureReady(player2, new OreplatePangolin());
        hullcarver.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(hullcarver);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(hullcarver.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Deathtouch also kills a larger attacker when Hullcarver blocks")
    void deathtouchKillsAttacker() {
        Permanent attacker = addCreatureReady(player1, new OreplatePangolin());
        addCreatureReady(player2, new Hullcarver());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Oreplate Pangolin");
        harness.assertInGraveyard(player2, "Hullcarver");
    }

    @Test
    @DisplayName("A zero-power Hullcarver deals no damage and does not kill its blocker")
    void zeroPowerDoesNotApplyDeathtouch() {
        Permanent hullcarver = addCreatureReady(player1, new Hullcarver());
        Permanent blocker = addCreatureReady(player2, new OreplatePangolin());
        hullcarver.setPowerModifier(-1);
        hullcarver.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hullcarver");
        harness.assertOnBattlefield(player2, "Oreplate Pangolin");
        assertThat(blocker.getMarkedDamage()).isZero();
    }
}
