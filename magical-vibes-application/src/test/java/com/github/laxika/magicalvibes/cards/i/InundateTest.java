package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CascadeBluffs;
import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.cards.s.ShorecrasherMimic;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Inundate.class, NettleSentinel.class, ShorecrasherMimic.class, CascadeBluffs.class})
class InundateTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all nonblue creatures to their owners' hands")
    void returnsNonblueCreatures() {
        harness.addToBattlefield(player1, new NettleSentinel());
        harness.addToBattlefield(player2, new NettleSentinel());
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new Inundate(), "{3}{U}{U}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Nettle Sentinel");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Nettle Sentinel");
    }

    @Test
    @DisplayName("Leaves blue creatures on the battlefield")
    void leavesBlueCreatures() {
        harness.addToBattlefield(player1, new ShorecrasherMimic());
        harness.addToBattlefield(player2, new NettleSentinel());
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new Inundate(), "{3}{U}{U}{U}");

        harness.passBothPriorities();

        // Blue creature stays; nonblue creature bounced
        harness.assertOnBattlefield(player1, "Shorecrasher Mimic");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Nettle Sentinel");
    }

    @Test
    @DisplayName("Does not return noncreature permanents")
    void doesNotReturnNoncreatures() {
        harness.addToBattlefield(player1, new CascadeBluffs());
        harness.addToBattlefield(player1, new NettleSentinel());
        harness.castFromHand(player1, new Inundate(), "{3}{U}{U}{U}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cascade Bluffs");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Nettle Sentinel");
    }

    @Test
    @DisplayName("Returns a controlled nonblue creature to its owner's hand")
    void returnsControlledCreatureToItsOwnersHand() {
        NettleSentinel ownedByPlayer2 = new NettleSentinel();
        ownedByPlayer2.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, ownedByPlayer2);
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new Inundate(), "{3}{U}{U}{U}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nettle Sentinel");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(ownedByPlayer2);
        assertThat(gd.playerHands.get(player2.getId())).contains(ownedByPlayer2);
    }
}
