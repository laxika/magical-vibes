package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoongloveExtractorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and loses 1 life when it attacks")
    void drawsCardAndLosesLifeWhenItAttacks() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        addCreatureReady(player1, new MoongloveExtractor());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }
}
