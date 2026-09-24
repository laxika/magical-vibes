package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FlametongueKavu;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MineSecurity.class, FlametongueKavu.class, GrizzlyBears.class})
class MineSecurityTest extends BaseCardTest {

    @Test
    void entersAndConjuresFlametongueKavuIntoTheTopEight() {
        harness.setLibrary(player1, List.of(
                filler("One"), filler("Two"), filler("Three"), filler("Four"),
                filler("Five"), filler("Six"), filler("Seven"), filler("Eight"),
                filler("Nine"), filler("Ten")));
        castMineSecurity();

        List<Card> library = gd.playerDecks.get(player1.getId());
        int conjuredIndex = findConjuredIndex(library);
        assertThat(library).hasSize(11);
        assertThat(conjuredIndex).isLessThan(8);
    }

    @Test
    void conjuredFlametongueKavuPerpetuallyHasAZeroManaAlternateCost() {
        harness.setLibrary(player1, List.of());
        castMineSecurity();

        harness.inMutationScope(() -> harness.getDrawService()
                .resolveDrawCards(gd, player1.getId(), 1));
        assertThat(gd.playerHands.get(player1.getId()).getFirst())
                .isInstanceOf(FlametongueKavu.class);

        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Flametongue Kavu");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void castMineSecurity() {
        harness.setHand(player1, List.of(new MineSecurity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int findConjuredIndex(List<Card> library) {
        for (int i = 0; i < library.size(); i++) {
            if (library.get(i) instanceof FlametongueKavu) {
                return i;
            }
        }
        return -1;
    }

    private Card filler(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }
}
