package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RipClanCrasher.class})
class RipClanCrasherTest extends BaseCardTest {

    @Test
    void canAttackTheTurnItEntersTheBattlefield() {
        harness.castFromHand(player1, new RipClanCrasher(), "{R}{G}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        Permanent crasher = findPermanent(player1, "Rip-Clan Crasher");
        assertThat(crasher.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void hasteDoesNotAllowAttackingWhileTapped() {
        harness.castFromHand(player1, new RipClanCrasher(), "{R}{G}");
        harness.passBothPriorities();

        Permanent crasher = findPermanent(player1, "Rip-Clan Crasher");
        crasher.tap();

        assertThat(als.canAttack(gd, crasher, player1.getId())).isFalse();
    }
}
