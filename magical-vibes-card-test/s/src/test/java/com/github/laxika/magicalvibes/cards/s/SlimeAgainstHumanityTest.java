package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BiogenicOoze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlimeAgainstHumanity.class, BiogenicOoze.class, GrizzlyBears.class})
class SlimeAgainstHumanityTest extends BaseCardTest {

    @Test
    void countsOwnedOozesAndNamedCardsInGraveyardAndExile() {
        harness.setGraveyard(player1, List.of(
                new BiogenicOoze(), new SlimeAgainstHumanity(), new GrizzlyBears()));
        harness.setExile(player1, List.of(
                new BiogenicOoze(), new SlimeAgainstHumanity(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new BiogenicOoze(), new SlimeAgainstHumanity()));
        harness.setExile(player2, List.of(new BiogenicOoze(), new SlimeAgainstHumanity()));
        harness.castFromHand(player1, new SlimeAgainstHumanity(), "{2}{G}");
        harness.passBothPriorities();

        Permanent ooze = findPermanent(player1, "Ooze");
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(ooze.getEffectivePower()).isEqualTo(6);
        assertThat(ooze.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    void createsTwoTwoOozeWithNoMatchingCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setExile(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new SlimeAgainstHumanity(), "{2}{G}");
        harness.passBothPriorities();

        Permanent ooze = findPermanent(player1, "Ooze");
        assertThat(ooze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ooze.getEffectivePower()).isEqualTo(2);
        assertThat(ooze.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void createsTokenWithTrample() {
        harness.castFromHand(player1, new SlimeAgainstHumanity(), "{2}{G}");
        harness.passBothPriorities();

        Permanent ooze = findPermanent(player1, "Ooze");
        assertThat(ooze.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, ooze, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void countsMatchingCardsAtResolution() {
        harness.castFromHand(player1, new SlimeAgainstHumanity(), "{2}{G}");
        harness.setGraveyard(player1, List.of(new SlimeAgainstHumanity()));
        harness.setExile(player1, List.of(new SlimeAgainstHumanity()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ooze").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(4);
    }

    @Test
    void doesNotCountFaceDownExiledCards() {
        gd.addToExile(player1.getId(), new SlimeAgainstHumanity(), null, true);
        gd.addToExile(player1.getId(), new BiogenicOoze(), null, true);
        gd.addToExile(player1.getId(), new SlimeAgainstHumanity(), null, false);

        harness.castFromHand(player1, new SlimeAgainstHumanity(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ooze").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
    }

    @Test
    void laterCastCountsEarlierSpellWithoutChangingEarlierToken() {
        harness.castFromHand(player1, new SlimeAgainstHumanity(), "{2}{G}");
        harness.passBothPriorities();
        Permanent firstOoze = findPermanent(player1, "Ooze");

        harness.castFromHand(player1, new SlimeAgainstHumanity(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ooze")).hasSize(2);
        assertThat(firstOoze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        Permanent secondOoze = findPermanents(player1, "Ooze").get(1);
        assertThat(secondOoze.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
