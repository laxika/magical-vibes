package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GaleriderSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VirulentSliver.class, GaleriderSliver.class, GrizzlyBears.class})
class VirulentSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Virulent Sliver gives poisonous 1 to itself and another Sliver")
    void sliversGivePoisonCounters() {
        Permanent virulentSliver = addCreatureReady(player1, new VirulentSliver());
        Permanent otherSliver = addCreatureReady(player1, new GaleriderSliver());
        virulentSliver.setAttacking(true);
        otherSliver.setAttacking(true);

        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Virulent Sliver gives poisonous 1 to opposing Slivers")
    void opposingSliversGetPoisonous() {
        addCreatureReady(player1, new VirulentSliver());
        Permanent opposingSliver = addCreatureReady(player2, new GaleriderSliver());
        opposingSliver.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Virulent Sliver does not give poisonous 1 to non-Slivers")
    void nonSliversDoNotGetPoisonous() {
        addCreatureReady(player1, new VirulentSliver());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setAttacking(true);

        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
