package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HagOfScouredThoughts.class, GrizzlyBears.class})
class HagOfScouredThoughtsTest extends BaseCardTest {

    @Test
    void upkeepMillsFiveThenExilesOpponentGraveyard() {
        addCreatureReady(player1, new HagOfScouredThoughts());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(6);
    }
}
