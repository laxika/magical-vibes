package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KalamaxTheStormsire.class, LightningBolt.class})
class KalamaxTheStormsireTest extends BaseCardTest {

    @Test
    @DisplayName("Copies the first instant while tapped and gets a counter when it copies")
    void copiesFirstInstantWhileTapped() {
        Permanent kalamax = addCreatureReady(player1, new KalamaxTheStormsire());
        kalamax.tap();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(kalamax.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Copies only the first instant each turn")
    void copiesOnlyFirstInstantEachTurn() {
        Permanent kalamax = addCreatureReady(player1, new KalamaxTheStormsire());
        kalamax.tap();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
        assertThat(kalamax.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not copy the first instant when it is untapped")
    void doesNotCopyUntappedFirstInstant() {
        Permanent kalamax = addCreatureReady(player1, new KalamaxTheStormsire());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(kalamax.getPlusOnePlusOneCounters()).isZero();
    }
}
