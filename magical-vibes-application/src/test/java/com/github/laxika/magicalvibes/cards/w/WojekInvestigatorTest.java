package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WojekInvestigator.class, GrizzlyBears.class})
class WojekInvestigatorTest extends BaseCardTest {

    @Test
    void investigatesForEachOpponentWithMoreCardsInHand() {
        harness.addToBattlefield(player1, new WojekInvestigator());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void comparesHandsWhenTheAbilityResolves() {
        harness.addToBattlefield(player1, new WojekInvestigator());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.setHand(player2, List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }
}
