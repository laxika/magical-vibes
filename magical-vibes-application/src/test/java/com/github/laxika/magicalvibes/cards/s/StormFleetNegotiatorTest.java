package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormFleetNegotiator.class, GrizzlyBears.class, Forest.class})
class StormFleetNegotiatorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a Map for each revealed nonland and each player draws")
    void createsMapsForNonlandsAndDraws() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new StormFleetNegotiator());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Map")).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking creates no Map for revealed lands but each player still draws")
    void skipsMapsForLandsAndDraws() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        addCreatureReady(player1, new StormFleetNegotiator());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Map")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
