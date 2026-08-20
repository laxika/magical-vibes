package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuakebringerTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents can't gain life while Quakebringer is on the battlefield")
    void opponentsCantGainLife() {
        harness.addToBattlefield(player1, new Quakebringer());

        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Battlefield upkeep trigger deals 2 damage even without another Giant")
    void battlefieldUpkeepTriggerDealsDamageWithoutAnotherGiant() {
        harness.addToBattlefield(player1, new Quakebringer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Graveyard upkeep trigger deals 2 damage while its controller controls a Giant")
    void graveyardUpkeepTriggerDealsDamageWithGiant() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new Quakebringer()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Graveyard upkeep trigger does not trigger without a Giant")
    void graveyardUpkeepTriggerDoesNotTriggerWithoutGiant() {
        harness.setGraveyard(player1, List.of(new Quakebringer()));

        advanceToUpkeep(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }
}
