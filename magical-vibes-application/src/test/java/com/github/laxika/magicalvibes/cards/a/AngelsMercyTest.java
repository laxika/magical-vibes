package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelsMercy.class})
class AngelsMercyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Angel's Mercy puts it on the stack")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new AngelsMercy(), "{2}{W}{W}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(AngelsMercy.class);
    }

    @Test
    @DisplayName("Angel's Mercy gains 7 life for its controller")
    void gains7Life() {
        harness.setLife(player1, 13);
        harness.castFromHand(player1, new AngelsMercy(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Angel's Mercy goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.castFromHand(player1, new AngelsMercy(), "{2}{W}{W}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Angel's Mercy");
    }

    @Test
    @DisplayName("Life gain is applied only on resolution and can exceed the starting total")
    void gainsLifeAboveStartingTotalOnlyOnResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 13);
        harness.castFromHand(player1, new AngelsMercy(), "{2}{W}{W}");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 13);
        harness.passBothPriorities();

        harness.assertLife(player1, 27);
        harness.assertLife(player2, 13);
    }

    @Test
    @DisplayName("The nonactive controller gains life when casting Angel's Mercy")
    void nonactiveControllerGainsLife() {
        harness.setLife(player1, 13);
        harness.setLife(player2, 8);
        harness.castFromHand(player2, new AngelsMercy(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Angel's Mercy");
    }
}
