package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TajuruPathwarden.class, GrizzlyBears.class})
class TajuruPathwardenTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance leaves it untapped after attacking")
    void vigilanceLeavesItUntappedAfterAttacking() {
        Permanent pathwarden = addCreatureReady(player1, new TajuruPathwarden());
        pathwarden.setAttacking(true);

        resolveCombat();

        assertThat(pathwarden.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        Permanent pathwarden = addCreatureReady(player1, new TajuruPathwarden());
        pathwarden.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
