package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BobReluctantHYDRAAgent.class, GrizzlyBears.class})
class BobReluctantHYDRAAgentTest extends BaseCardTest {

    @Test
    void attackingAloneReturnsBobAndChangesLifeTotals() {
        Permanent bob = addCreatureReady(player1, new BobReluctantHYDRAAgent());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertInHand(player1, "Bob, Reluctant HYDRA Agent");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bob);
    }

    @Test
    void attackingWithAnotherCreatureDoesNotTrigger() {
        Permanent bob = addCreatureReady(player1, new BobReluctantHYDRAAgent());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bob);
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(
                card -> card.getName().equals("Bob, Reluctant HYDRA Agent"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
