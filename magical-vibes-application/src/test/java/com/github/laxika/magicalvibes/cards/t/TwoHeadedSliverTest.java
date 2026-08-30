package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwoHeadedSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class TwoHeadedSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Two-Headed Sliver has menace itself")
    void grantsMenaceToItself() {
        Permanent sliver = addCreatureReady(player1, new TwoHeadedSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("All Slivers have menace")
    void grantsMenaceToAllSlivers() {
        addCreatureReady(player1, new TwoHeadedSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Non-Sliver creatures do not have menace from Two-Headed Sliver")
    void doesNotGrantMenaceToNonSlivers() {
        addCreatureReady(player1, new TwoHeadedSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }
}
