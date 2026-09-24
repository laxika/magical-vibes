package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sunder.class, Forest.class, Island.class, GloriousAnthem.class, CoralMerfolk.class})
class SunderTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all lands on the battlefield to their owners' hands")
    void returnsAllLandsToTheirOwnersHands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new Sunder(), "{3}{U}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Forest");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Island");
    }

    @Test
    @DisplayName("Returns a stolen land to its owner's hand")
    void returnsStolenLandToItsOwnerHand() {
        Permanent stolenIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        gd.stolenCreatures.put(stolenIsland.getId(), player1.getId());
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new Sunder(), "{3}{U}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Island");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Leaves nonland permanents on the battlefield")
    void leavesNonlandPermanentsOnBattlefield() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new CoralMerfolk());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.castFromHand(player1, new Sunder(), "{3}{U}{U}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Coral Merfolk");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactly("Forest");
    }
}
