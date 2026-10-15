package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CityOfDeath;
import com.github.laxika.magicalvibes.cards.s.SusanForeman;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IanChesterton.class, CityOfDeath.class, SusanForeman.class})
class IanChestertonTest extends BaseCardTest {

    @Test
    void givesSagaSpellsReplicateAtTheirManaCost() {
        harness.addToBattlefield(player1, new IanChesterton());
        harness.setHand(player1, List.of(new CityOfDeath()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantmentWithRepeatedCosts(player1, 0, List.of("{2}{G}"));
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
    }

    @Test
    void doesNotGiveReplicateToNonSagaSpells() {
        harness.addToBattlefield(player1, new IanChesterton());
        harness.castFromHand(player1, new SusanForeman(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Susan Foreman")).hasSize(1);
    }

    @Test
    void repeatedPaymentsCreateSeparateSagaTokensWithTheirFirstChapters() {
        harness.addToBattlefield(player1, new IanChesterton());
        harness.setHand(player1, List.of(new CityOfDeath()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantmentWithRepeatedCosts(player1, 0, List.of("{2}{G}", "{2}{G}"));
        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        for (int i = 0; i < 6; i++) {
            harness.passBothPriorities();
        }

        assertThat(findPermanents(player1, "City of Death")).hasSize(3)
                .allSatisfy(saga -> assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1));
        assertThat(findPermanents(player1, "City of Death").stream()
                .filter(saga -> saga.getCard().isToken())).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentIanDoesNotGiveYourSagaReplicate() {
        harness.addToBattlefield(player2, new IanChesterton());
        harness.castFromHand(player1, new CityOfDeath(), "{2}{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "City of Death")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void unpaidReplicateCreatesNoCopies() {
        harness.addToBattlefield(player1, new IanChesterton());
        harness.castFromHand(player1, new CityOfDeath(), "{2}{G}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "City of Death")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

}
