package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FlametongueKavu;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MineSecurity.class, FlametongueKavu.class})
class MineSecurityTest extends BaseCardTest {

    @Test
    void entersAndConjuresFlametongueKavuIntoTheTopEight() {
        harness.setLibrary(player1, List.of(
                new MineSecurity(), new MineSecurity(), new MineSecurity(), new MineSecurity(),
                new MineSecurity(), new MineSecurity(), new MineSecurity(), new MineSecurity(),
                new MineSecurity(), new MineSecurity()));
        castMineSecurity();

        List<Card> library = gd.playerDecks.get(player1.getId());
        int conjuredIndex = findConjuredIndex(library);
        assertThat(library).hasSize(11);
        assertThat(conjuredIndex).isBetween(0, 7);
    }

    @Test
    void conjuredFlametongueKavuPerpetuallyHasAZeroManaAlternateCost() {
        harness.setLibrary(player1, List.of());
        castMineSecurity();

        harness.inMutationScope(() -> harness.getDrawService()
                .resolveDrawCards(gd, player1.getId(), 1));
        assertThat(gd.playerHands.get(player1.getId()).getFirst())
                .isInstanceOf(FlametongueKavu.class);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new MineSecurity());
        harness.castWithAlternateCost(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Flametongue Kavu");
        harness.assertInGraveyard(player2, "Mine Security");
    }

    @Test
    void conjuresIntoALibraryWithFewerThanEightCardsWithoutLosingExistingCards() {
        Card first = new MineSecurity();
        Card second = new MineSecurity();
        harness.setLibrary(player1, List.of(first, second));

        castMineSecurity();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(findConjuredIndex(library)).isBetween(0, 2);
        assertThat(library.stream().filter(card -> !(card instanceof FlametongueKavu)).toList())
                .containsExactly(first, second);
    }

    @Test
    void conjuresEvenWhenMineSecurityLeavesBeforeItsTriggerResolves() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new MineSecurity(), "{1}{R}");
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Mine Security");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, source));

        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).singleElement()
                .isInstanceOf(FlametongueKavu.class);
    }

    @Test
    void zeroManaAlternateCostSurvivesReturningTheConjuredCreatureToHand() {
        harness.setLibrary(player1, List.of());
        castMineSecurity();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 1));
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new MineSecurity());
        harness.castWithAlternateCost(player1, 0, firstTarget.getId());
        resolveAllTriggers();

        Permanent kavu = findPermanent(player1, "Flametongue Kavu");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, kavu));
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new MineSecurity());
        harness.castWithAlternateCost(player1, 0, secondTarget.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Flametongue Kavu");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player2, "Mine Security");
    }

    private void castMineSecurity() {
        harness.castFromHand(player1, new MineSecurity(), "{1}{R}");
        resolveAllTriggers();
    }

    private int findConjuredIndex(List<Card> library) {
        for (int i = 0; i < library.size(); i++) {
            if (library.get(i) instanceof FlametongueKavu) {
                return i;
            }
        }
        return -1;
    }
}
