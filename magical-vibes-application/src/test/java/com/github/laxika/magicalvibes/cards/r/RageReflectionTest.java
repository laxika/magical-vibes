package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RageReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's creatures have double strike")
    void controllersCreaturesHaveDoubleStrike() {
        harness.addToBattlefield(player1, new RageReflection());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Opponent's creatures do NOT have double strike")
    void opponentsCreaturesDoNotHaveDoubleStrike() {
        harness.addToBattlefield(player1, new RageReflection());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
