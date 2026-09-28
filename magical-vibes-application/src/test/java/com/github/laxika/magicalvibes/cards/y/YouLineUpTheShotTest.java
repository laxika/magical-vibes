package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Plummet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YouLineUpTheShot.class, Forest.class, Naturalize.class, Plummet.class})
class YouLineUpTheShotTest extends BaseCardTest {

    @Test
    void conjuresPlummet() {
        cast(0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Plummet);
    }

    @Test
    void conjuresNaturalize() {
        cast(1);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Naturalize);
    }

    @Test
    void drawsACard() {
        harness.setLibrary(player1, List.of(new Forest()));
        cast(2);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Forest);
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(new YouLineUpTheShot()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castModalInstant(player1, 0, modeIndex, List.of());
        harness.passBothPriorities();
    }
}
