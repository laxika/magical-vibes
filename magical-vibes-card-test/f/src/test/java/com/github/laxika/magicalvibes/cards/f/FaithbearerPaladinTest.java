package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaithbearerPaladin.class})
class FaithbearerPaladinTest extends BaseCardTest {

    @Test
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent paladin = addCreatureReady(player1, new FaithbearerPaladin());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(paladin)));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void bothControllersGainLifeFromDamageToCreaturesInBlockedCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new FaithbearerPaladin());
        Permanent blocker = addCreatureReady(player2, new FaithbearerPaladin());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
        harness.assertOnBattlefield(player1, "Faithbearer Paladin");
        harness.assertOnBattlefield(player2, "Faithbearer Paladin");
    }
}
