package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoldwyrAggressorTest extends BaseCardTest {

    @Test
    @DisplayName("Other Giants you control have double strike")
    void grantsDoubleStrikeToOtherGiantsYouControl() {
        harness.addToBattlefield(player1, new BoldwyrAggressor());
        harness.addToBattlefield(player1, new HillGiant());

        Permanent hillGiant = findPermanent(player1, "Hill Giant");

        assertThat(gqs.hasKeyword(gd, hillGiant, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant double strike to non-Giants")
    void doesNotGrantDoubleStrikeToNonGiants() {
        harness.addToBattlefield(player1, new BoldwyrAggressor());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent grizzlyBears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, grizzlyBears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant double strike to an opponent's Giants")
    void doesNotGrantDoubleStrikeToOpponentsGiants() {
        harness.addToBattlefield(player1, new BoldwyrAggressor());
        harness.addToBattlefield(player2, new HillGiant());

        Permanent opponentGiant = findPermanent(player2, "Hill Giant");

        assertThat(gqs.hasKeyword(gd, opponentGiant, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double strike is lost when Boldwyr Aggressor leaves the battlefield")
    void doubleStrikeIsLostWhenAggressorLeaves() {
        harness.addToBattlefield(player1, new BoldwyrAggressor());
        harness.addToBattlefield(player1, new HillGiant());

        Permanent hillGiant = findPermanent(player1, "Hill Giant");
        assertThat(gqs.hasKeyword(gd, hillGiant, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Boldwyr Aggressor"));

        assertThat(gqs.hasKeyword(gd, hillGiant, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
