package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EumidianTerrabotanist.class, Forest.class})
class EumidianTerrabotanistTest extends BaseCardTest {

    @Test
    void gainsLifeWhenYouPlayALand() {
        harness.addToBattlefield(player1, new EumidianTerrabotanist());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void doesNotTriggerForAnOpponentsLand() {
        harness.addToBattlefield(player1, new EumidianTerrabotanist());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
