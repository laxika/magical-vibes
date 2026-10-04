package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.v.VoyagesEnd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrayMerchantOfAsphodel.class, VoyagesEnd.class})
@DisplayName("Gray Merchant of Asphodel")
class GrayMerchantOfAsphodelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB counts black devotion including itself and drains that amount")
    void etbCountsBlackDevotionAndDrainsThatAmount() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrayMerchantOfAsphodel());
        harness.setHand(player1, List.of(new GrayMerchantOfAsphodel()));
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 14);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Devotion excludes cards in hand, graveyards, libraries, and opposing permanents")
    void devotionCountsOnlyControlledPermanents() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new GrayMerchantOfAsphodel()));
        harness.setLibrary(player1, List.of(new GrayMerchantOfAsphodel()));
        harness.addToBattlefield(player2, new GrayMerchantOfAsphodel());
        harness.setHand(player1, List.of(new GrayMerchantOfAsphodel(), new GrayMerchantOfAsphodel()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 12);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trigger still resolves with zero devotion after its source leaves")
    void sourceLeavingCanReduceDevotionToZero() {
        castMerchantAndReturnItBeforeTriggerResolves();
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Gray Merchant of Asphodel");
        harness.assertNotOnBattlefield(player1, "Gray Merchant of Asphodel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Trigger uses current devotion rather than its departed source's mana cost")
    void sourceLeavingStillDrainsForRemainingDevotion() {
        castMerchantAndReturnItBeforeTriggerResolves();
        harness.addToBattlefield(player1, new GrayMerchantOfAsphodel());
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    private void castMerchantAndReturnItBeforeTriggerResolves() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GrayMerchantOfAsphodel()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Gray Merchant of Asphodel"));
        assertThat(gd.stack).hasSize(1);
    }
}
