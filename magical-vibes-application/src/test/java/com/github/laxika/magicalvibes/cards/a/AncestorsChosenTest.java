package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({AncestorsChosen.class})
class AncestorsChosenTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 1 life for each card in its controller's graveyard")
    void gainsLifeForEachCardInControllerGraveyard() {
        harness.setGraveyard(player1, List.of(
                new AncestorsChosen(), new AncestorsChosen(), new AncestorsChosen()));
        harness.setGraveyard(player2, List.of(new AncestorsChosen(), new AncestorsChosen()));
        harness.setLife(player1, 10);

        harness.castFromHand(player1, new AncestorsChosen(), "{5}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB gains no life with an empty controller graveyard")
    void gainsNoLifeWithEmptyControllerGraveyard() {
        harness.setGraveyard(player1, List.of());

        harness.castFromHand(player1, new AncestorsChosen(), "{5}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
