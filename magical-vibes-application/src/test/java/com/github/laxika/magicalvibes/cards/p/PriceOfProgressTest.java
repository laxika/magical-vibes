package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({PriceOfProgress.class, FieldOfRuin.class, Forest.class})
class PriceOfProgressTest extends BaseCardTest {

    @Test
    @DisplayName("Deals each player twice the damage for their own nonbasic lands")
    void dealsDamageBasedOnEachPlayersNonbasicLands() {
        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new PriceOfProgress()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Basic lands do not count")
    void basicLandsDoNotCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new PriceOfProgress()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Counts nonbasic lands when the spell resolves")
    void countsNonbasicLandsAtResolution() {
        harness.setHand(player1, List.of(new PriceOfProgress()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0);

        harness.addToBattlefield(player1, new FieldOfRuin());
        harness.addToBattlefield(player2, new FieldOfRuin());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }
}
