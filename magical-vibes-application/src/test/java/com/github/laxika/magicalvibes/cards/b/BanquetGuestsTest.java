package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BanquetGuests.class, BagEndBanquet.class})
class BanquetGuestsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with twice X +1/+1 counters")
    void entersWithTwiceXPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new BanquetGuests()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent guests = findPermanent(player1, "Banquet Guests");
        assertThat(guests.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Food affinity reduces the generic casting cost")
    void foodAffinityReducesGenericCastingCost() {
        createFoods();

        harness.setHand(player1, List.of(new BanquetGuests()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Sacrificing a Food grants indestructible until end of turn")
    void sacrificingFoodGrantsIndestructibleUntilEndOfTurn() {
        createFoods();
        Permanent guests = harness.addToBattlefieldAndReturn(player1, new BanquetGuests());
        guests.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent food = findPermanent(player1, "Food");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guests), null, null);
        harness.handlePermanentChosen(player1, food.getId());
        harness.passBothPriorities();

        assertThat(guests.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(guests.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    private void createFoods() {
        harness.setHand(player1, List.of(new BagEndBanquet()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
