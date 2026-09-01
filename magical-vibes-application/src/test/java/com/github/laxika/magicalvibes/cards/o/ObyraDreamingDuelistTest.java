package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FaerieInvaders;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObyraDreamingDuelist.class, FaerieInvaders.class, GrizzlyBears.class})
class ObyraDreamingDuelistTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life when another Faerie enters under your control")
    void drainsEachOpponentForAnotherFaerie() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.enterBattlefieldAndReturn(player1, new ObyraDreamingDuelist());

        harness.enterBattlefieldAndReturn(player1, new FaerieInvaders());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger for a non-Faerie or an opponent's Faerie")
    void doesNotTriggerForNonFaerieOrOpponentFaerie() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.enterBattlefieldAndReturn(player1, new ObyraDreamingDuelist());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new FaerieInvaders());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
