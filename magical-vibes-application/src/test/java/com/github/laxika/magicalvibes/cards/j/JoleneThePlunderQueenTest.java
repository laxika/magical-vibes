package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Treasure;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JoleneThePlunderQueen.class, GrizzlyBears.class, Treasure.class})
class JoleneThePlunderQueenTest extends BaseCardTest {

    @Test
    void createsTwoTreasuresWhenAttackingAnOpponent() {
        addCreatureReady(player1, new JoleneThePlunderQueen());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    void doesNotCreateTreasureWhenOpponentAttacksJolene() {
        addCreatureReady(player1, new JoleneThePlunderQueen());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Treasure")).isZero();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void sacrificesFiveTreasuresForFiveCounters() {
        Permanent jolene = addCreatureReady(player1, new JoleneThePlunderQueen());
        for (int i = 0; i < 5; i++) {
            addTreasure(player1);
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(jolene.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    private Permanent addTreasure(Player player) {
        Permanent treasure = new Permanent(new Treasure());
        treasure.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(treasure);
        return treasure;
    }

}
