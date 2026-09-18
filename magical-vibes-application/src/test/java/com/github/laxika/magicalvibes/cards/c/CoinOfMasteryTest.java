package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.ManaVault;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoinOfMastery.class, ManaVault.class, GrizzlyBears.class})
class CoinOfMasteryTest extends BaseCardTest {

    @Test
    void artifactManaSpentOnCreatureAddsThatManyCounters() {
        harness.addToBattlefield(player1, new CoinOfMastery());
        Permanent vault = addReadyVault(player1);
        gs.tapPermanent(gd, player1, gd.playerBattlefields.get(player1.getId()).indexOf(vault));

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void tappingCreatesTreasureToken() {
        Permanent coin = harness.addToBattlefieldAndReturn(player1, new CoinOfMastery());
        int coinIndex = gd.playerBattlefields.get(player1.getId()).indexOf(coin);

        harness.activateAbility(player1, coinIndex, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private Permanent addReadyVault(Player player) {
        Permanent vault = new Permanent(new ManaVault());
        vault.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(vault);
        return vault;
    }
}
