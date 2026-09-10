package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CanyonWildcat;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Kindle.class, CanyonWildcat.class})
class KindleTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage when no Kindle is in any graveyard")
    void dealsTwoDamageWithNoKindlesInGraveyards() {
        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Counts Kindle cards in every player's graveyard")
    void countsKindlesInAllGraveyards() {
        harness.setGraveyard(player1, List.of(new Kindle()));
        harness.setGraveyard(player2, List.of(new Kindle()));

        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // 2 + 2 other Kindles = 4 damage
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Does not count other cards in graveyards")
    void ignoresOtherCards() {
        Card other = new CanyonWildcat();
        harness.setGraveyard(player1, List.of(other));

        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Can target a creature and kills it with the boosted damage")
    void damagesTargetCreature() {
        harness.setGraveyard(player1, List.of(new Kindle()));
        var wildcat = harness.addToBattlefieldAndReturn(player2, new CanyonWildcat());

        harness.setHand(player1, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, wildcat.getId());

        // 2 + 1 = 3 damage kills a 2/1
        harness.assertInGraveyard(player2, "Canyon Wildcat");
    }

    @Test
    @DisplayName("Counts a Kindle that resolves before it")
    void countsKindleThatResolvedWhileItWasOnTheStack() {
        harness.setHand(player1, List.of(new Kindle()));
        harness.setHand(player2, List.of(new Kindle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player2, 0, player1.getId());

        harness.passBothPriorities();
        harness.assertLife(player1, 18);

        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The resolving copy does not count itself")
    void resolvingCopyDoesNotCountItself() {
        harness.setHand(player1, List.of(new Kindle(), new Kindle()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 18);

        // The first Kindle is now in the graveyard, so the second deals 3.
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 15);
    }
}
