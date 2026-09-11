package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FriendlyTeddy.class, GrizzlyBears.class})
class FriendlyTeddyTest extends BaseCardTest {

    @Test
    void whenItDiesEachPlayerDrawsACard() {
        GrizzlyBears player1Draw = new GrizzlyBears();
        GrizzlyBears player2Draw = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(player1Draw));
        harness.setLibrary(player2, List.of(player2Draw));

        Permanent teddy = harness.addToBattlefieldAndReturn(player1, new FriendlyTeddy());
        teddy.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Friendly Teddy");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Draw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Draw);
    }
}
