package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HagOfDarkDuress.class, GrizzlyBears.class})
class HagOfDarkDuressTest extends BaseCardTest {

    @Test
    void upkeepMakesOpponentDiscard() {
        addCreatureReady(player1, new HagOfDarkDuress());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }
}
