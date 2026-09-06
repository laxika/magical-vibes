package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkybeastTracker.class, AirElemental.class, HillGiant.class})
class SkybeastTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when its controller casts a spell with mana value 5")
    void createsFoodForSpellWithManaValueFive() {
        castTracker();
        castAirElemental();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Does not create a Food token for a spell with mana value 4")
    void doesNotCreateFoodForSpellWithManaValueFour() {
        castTracker();
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("The created Food token can be sacrificed to gain 3 life")
    void createdFoodCanBeSacrificedForLife() {
        castTracker();
        castAirElemental();
        resolveAllTriggers();

        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, foodIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a spell with mana value 5")
    void doesNotTriggerForOpponentsSpell() {
        castTracker();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AirElemental()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    private void castTracker() {
        harness.setHand(player1, List.of(new SkybeastTracker()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void castAirElemental() {
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
