package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteadfastPaladin.class})
class SteadfastPaladinTest extends BaseCardTest {

    @Test
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new SteadfastPaladin());

        declareAttackers(player1, List.of(0));
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void bothControllersGainLifeWhenPaladinsDealLethalDamageToEachOther() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SteadfastPaladin());
        harness.addToBattlefield(player2, new SteadfastPaladin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
        harness.assertNotOnBattlefield(player1, "Steadfast Paladin");
        harness.assertNotOnBattlefield(player2, "Steadfast Paladin");
        harness.assertInGraveyard(player1, "Steadfast Paladin");
        harness.assertInGraveyard(player2, "Steadfast Paladin");
    }

    @Test
    void blockingPaladinGainsLifeOnlyForItsControllerAtLowLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 1);
        addCreatureReady(player1, new SteadfastPaladin());
        addCreatureReady(player1, new SteadfastPaladin());
        harness.addToBattlefield(player2, new SteadfastPaladin());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Steadfast Paladin");
        harness.assertInGraveyard(player2, "Steadfast Paladin");
    }
}
