package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoiceOfMany.class, Forest.class, GrizzlyBears.class})
class VoiceOfManyTest extends BaseCardTest {

    @Test
    void drawsForEachOpponentWithFewerCreatures() {
        stockLibrary(3);

        harness.castFromHand(player1, new VoiceOfMany(), "{2}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawWhenOpponentControlsTheSameNumberOfCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        stockLibrary(3);

        harness.castFromHand(player1, new VoiceOfMany(), "{2}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void countsCreaturesWhenTheEtbAbilityResolves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        stockLibrary(3);

        harness.castFromHand(player1, new VoiceOfMany(), "{2}{G}{G}");
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void stockLibrary(int count) {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new Forest());
        }
    }
}
