package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnionOfTheThirdPathTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card, then gains life equal to the resulting hand size")
    void drawsThenGainsLifeBasedOnResultingHandSize() {
        harness.setHand(player1, List.of(
                new UnionOfTheThirdPath(), new Forest(), new Mountain(), new Plains()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setLife(player1, 10);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
