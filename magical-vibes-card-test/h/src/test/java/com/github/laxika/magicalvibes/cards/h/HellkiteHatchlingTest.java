package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WildLeotau;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HellkiteHatchling.class, WildLeotau.class})
class HellkiteHatchlingTest extends BaseCardTest {

    private void castHellkiteHatchling() {
        harness.castFromHand(player1, new HellkiteHatchling(), "{2}{R}{G}");
        harness.passBothPriorities(); // resolve creature spell -> devour choice
    }

    private Permanent hellkite() {
        return findPermanent(player1, "Hellkite Hatchling");
    }

    @Test
    @DisplayName("Devouring a creature enters with a +1/+1 counter and gains flying and trample")
    void devourGrantsFlyingAndTrample() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new WildLeotau());

        castHellkiteHatchling();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        Permanent hellkite = hellkite();
        assertThat(hellkite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Devouring nothing enters with no counters and has neither flying nor trample")
    void devourNoneNoKeywords() {
        harness.addToBattlefieldAndReturn(player1, new WildLeotau());

        castHellkiteHatchling();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        Permanent hellkite = hellkite();
        assertThat(hellkite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Devour can sacrifice multiple creatures while leaving others alone")
    void devoursMultipleCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WildLeotau());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WildLeotau());
        Permanent kept = harness.addToBattlefieldAndReturn(player1, new WildLeotau());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new WildLeotau());

        castHellkiteHatchling();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(hellkite().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kept).doesNotContain(first, second);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gqs.hasKeyword(gd, hellkite(), Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, hellkite(), Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature alone is not available for devour")
    void entersWithoutDevouringWhenOnlyOpponentHasCreatures() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new WildLeotau());

        castHellkiteHatchling();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
        assertThat(hellkite().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, hellkite(), Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, hellkite(), Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Removing devour counters does not remove flying or trample")
    void retainsKeywordsWithoutCounters() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new WildLeotau());
        castHellkiteHatchling();
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        hellkite().setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, hellkite(), Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, hellkite(), Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Counters without devouring do not grant flying or trample")
    void unrelatedCountersDoNotGrantKeywords() {
        castHellkiteHatchling();

        hellkite().setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, hellkite(), Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, hellkite(), Keyword.TRAMPLE)).isFalse();
    }
}
