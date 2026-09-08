package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Typhoon.class, Island.class, Forest.class})
class TyphoonTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage to each opponent equal to that opponent's Island count")
    void dealsDamageBasedOnEachOpponentsIslandCount() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Forest());
        castTyphoon();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals no damage when an opponent controls no Islands")
    void dealsNoDamageWithoutIslands() {
        harness.addToBattlefield(player2, new Forest());
        castTyphoon();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counts Islands when the spell resolves")
    void countsIslandsAtResolution() {
        harness.setHand(player1, List.of(new Typhoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);

        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void castTyphoon() {
        harness.setHand(player1, List.of(new Typhoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
