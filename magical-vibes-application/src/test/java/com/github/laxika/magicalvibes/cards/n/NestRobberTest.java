package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NestRobber.class})
class NestRobberTest extends BaseCardTest {

    @Test
    void canAttackTheTurnItEnters() {
        harness.castFromHand(player1, new NestRobber(), "{1}{R}");
        harness.passBothPriorities();

        Permanent robber = findPermanent(player1, "Nest Robber");
        assertThat(robber.isSummoningSick()).isTrue();

        declareAttackers(List.of(0));

        assertThat(robber.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void hasteDoesNotAllowAttackingWhileTapped() {
        harness.castFromHand(player1, new NestRobber(), "{1}{R}");
        harness.passBothPriorities();

        Permanent robber = findPermanent(player1, "Nest Robber");
        robber.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
