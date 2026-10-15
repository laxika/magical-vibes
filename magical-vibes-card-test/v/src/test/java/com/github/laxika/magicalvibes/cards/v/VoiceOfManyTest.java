package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoiceOfMany.class, Forest.class, GrizzlyBears.class})
class VoiceOfManyTest extends BaseCardTest {

    @Test
    void drawsForEachOpponentWithFewerCreatures() {
        stockLibrary(3);

        harness.castFromHand(player1, new VoiceOfMany(), "{2}{G}{G}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawWhenOpponentControlsTheSameNumberOfCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        stockLibrary(3);

        harness.castFromHand(player1, new VoiceOfMany(), "{2}{G}{G}");
        resolveAllTriggers();

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

    @Test
    void doesNotDrawWhenOpponentControlsMoreCreatures() {
        harness.addToBattlefield(player2, new VoiceOfMany());
        harness.addToBattlefield(player2, new VoiceOfMany());
        stockLibrary(3);

        harness.castFromHand(player1, new VoiceOfMany(), "{2}{G}{G}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsOnlyOneCardForOneOpponentRegardlessOfCreatureDifference() {
        harness.addToBattlefield(player1, new VoiceOfMany());
        harness.addToBattlefield(player1, new VoiceOfMany());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        stockLibrary(3);

        harness.castFromHand(player1, new VoiceOfMany(), "{2}{G}{G}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawIfOpponentGainsACreatureBeforeTriggerResolves() {
        stockLibrary(3);

        harness.castFromHand(player1, new VoiceOfMany(), "{2}{G}{G}");
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new VoiceOfMany());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void stockLibrary(int count) {
        harness.setLibrary(player1, IntStream.range(0, count).mapToObj(i -> new Forest()).toList());
    }
}
