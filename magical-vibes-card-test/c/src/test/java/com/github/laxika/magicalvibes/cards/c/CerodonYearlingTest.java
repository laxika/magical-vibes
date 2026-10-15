package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CerodonYearling.class})
class CerodonYearlingTest extends BaseCardTest {

    @Test
    void canAttackImmediatelyWithoutTapping() {
        harness.castFromHand(player1, new CerodonYearling(), "{R}{W}");
        harness.passBothPriorities();
        Permanent yearling = findPermanent(player1, "Cerodon Yearling");

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(yearling.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void canBlockAfterAttackingWithVigilance() {
        Permanent yearling = addCreatureReady(player1, new CerodonYearling());
        declareAttackers(List.of(0));
        resolveCombat();
        assertThat(yearling.isTapped()).isFalse();

        addCreatureReady(player2, new CerodonYearling());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(yearling.isBlocking()).isTrue();
    }

    @Test
    void vigilanceAndHasteDoNotAllowAttackingWhileTapped() {
        Permanent yearling = harness.addToBattlefieldAndReturn(player1, new CerodonYearling());
        yearling.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(yearling.isAttacking()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
