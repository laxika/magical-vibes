package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FarmerCotton;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BanquetGuests.class, FarmerCotton.class})
class BanquetGuestsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with twice X +1/+1 counters")
    void entersWithTwiceXPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new BanquetGuests()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 2);
        resolveAllTriggers();

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
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Banquet Guests").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(4);
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
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);
        assertThat(guests.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        resolveAllTriggers();

        assertThat(guests.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(food);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(guests.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Affinity reduces only the payable generic portion and preserves chosen X")
    void affinityPreservesXWhenGenericManaRemainsPayable() {
        createFoods();
        harness.setHand(player1, List.of(new BanquetGuests()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 5);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Banquet Guests").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(10);
    }

    @Test
    @DisplayName("X zero gives no counters even when Foods reduce the cost")
    void zeroXDiesDespiteAffinity() {
        createFoods();
        harness.setHand(player1, List.of(new BanquetGuests()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Banquet Guests");
        harness.assertInGraveyard(player1, "Banquet Guests");
    }

    @Test
    @DisplayName("Opponent-controlled Foods do not reduce the casting cost")
    void opposingFoodsDoNotReduceCastingCost() {
        createFoods();
        List<Permanent> foods = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Food"))
                .toList();
        gd.playerBattlefields.get(player1.getId()).removeAll(foods);
        gd.playerBattlefields.get(player2.getId()).addAll(foods);
        harness.setHand(player1, List.of(new BanquetGuests()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Banquet Guests").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(6);
    }

    @Test
    @DisplayName("Indestructible cannot be activated without a Food to sacrifice")
    void cannotActivateWithoutFood() {
        Permanent guests = harness.addToBattlefieldAndReturn(player1, new BanquetGuests());
        guests.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(guests.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        assertThat(gd.stack).isEmpty();
    }

    private void createFoods() {
        harness.setHand(player1, List.of(new FarmerCotton()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 3);
        resolveAllTriggers();
    }
}
