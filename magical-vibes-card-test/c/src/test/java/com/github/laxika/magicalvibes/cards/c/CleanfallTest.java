package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HondenOfCleansingFire;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cleanfall.class, HondenOfCleansingFire.class, WanderingOnes.class})
class CleanfallTest extends BaseCardTest {

    @Test
    void destroysAllEnchantmentsControlledByBothPlayers() {
        harness.addToBattlefield(player1, new HondenOfCleansingFire());
        harness.addToBattlefield(player2, new HondenOfCleansingFire());

        harness.castFromHand(player1, new Cleanfall(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Honden of Cleansing Fire");
        harness.assertNotOnBattlefield(player2, "Honden of Cleansing Fire");
        harness.assertInGraveyard(player1, "Honden of Cleansing Fire");
        harness.assertInGraveyard(player2, "Honden of Cleansing Fire");
    }

    @Test
    void doesNotDestroyNonEnchantments() {
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.addToBattlefield(player2, new WanderingOnes());

        harness.castFromHand(player1, new Cleanfall(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wandering Ones");
        harness.assertOnBattlefield(player2, "Wandering Ones");
    }

    @Test
    void resolvesWithoutEnchantmentsOnTheBattlefield() {
        harness.castFromHand(player1, new Cleanfall(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cleanfall");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void leavesEnchantmentCardsInHandAndLibraryUntouched() {
        HondenOfCleansingFire libraryCard = new HondenOfCleansingFire();
        harness.setHand(player2, List.of(new HondenOfCleansingFire()));
        harness.setLibrary(player2, List.of(libraryCard));
        harness.addToBattlefield(player2, new HondenOfCleansingFire());

        harness.castFromHand(player1, new Cleanfall(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Honden of Cleansing Fire");
        harness.assertInGraveyard(player2, "Honden of Cleansing Fire");
        harness.assertInHand(player2, "Honden of Cleansing Fire");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        harness.assertInGraveyard(player1, "Cleanfall");
    }
}
