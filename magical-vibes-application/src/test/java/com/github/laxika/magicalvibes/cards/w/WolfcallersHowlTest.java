package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WolfcallersHowl.class, GrizzlyBears.class})
class WolfcallersHowlTest extends BaseCardTest {

    @Test
    void createsOneWolfForOpponentWithAtLeastFourCardsInHand() {
        harness.addToBattlefield(player1, new WolfcallersHowl());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).hasSize(1);
    }

    @Test
    void doesNotCreateWolfForOpponentWithFewerThanFourCardsInHand() {
        harness.addToBattlefield(player1, new WolfcallersHowl());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }

    @Test
    void checksOpponentsHandsWhenTheAbilityResolves() {
        harness.addToBattlefield(player1, new WolfcallersHowl());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wolf")).isEmpty();
    }
}
