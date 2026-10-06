package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HamletGlutton;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IntoTheFaeCourt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkybeastTracker.class, AirElemental.class, HillGiant.class, HamletGlutton.class, IntoTheFaeCourt.class})
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
        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
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
        harness.castFromHand(player2, new AirElemental(), "{3}{U}{U}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    private void castTracker() {
        harness.castFromHand(player1, new SkybeastTracker(), "{3}{G}");
        harness.passBothPriorities();
    }

    private void castAirElemental() {
        harness.castFromHand(player1, new AirElemental(), "{3}{U}{U}");
    }

    @Test
    @DisplayName("Creates Food before the triggering spell resolves")
    void createsFoodBeforeSpellResolves() {
        castTracker();
        castAirElemental();

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        harness.assertNotOnBattlefield(player1, "Air Elemental");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Creates Food for a spell with mana value greater than five")
    void createsFoodForSpellAboveThreshold() {
        castTracker();
        harness.castFromHand(player1, new HamletGlutton(), "{5}{G}{G}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Triggers for each qualifying spell in the same turn")
    void triggersForEachQualifyingSpell() {
        castTracker();
        castAirElemental();
        resolveAllTriggers();
        castAirElemental();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Tracker creates its own Food token")
    void eachTrackerTriggersIndependently() {
        castTracker();
        castTracker();
        assertThat(countPermanents(player1, "Food")).isZero();
        castAirElemental();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
    }

    @Test
    @DisplayName("Entering without being cast does not create Food")
    void enteringWithoutCastingDoesNotTrigger() {
        castTracker();
        harness.addToBattlefield(player1, new AirElemental());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("A qualifying noncreature spell also creates Food")
    void noncreatureSpellCreatesFood() {
        castTracker();
        harness.setLibrary(player1, List.of(new SkybeastTracker(), new SkybeastTracker(), new SkybeastTracker()));
        harness.castFromHand(player1, new IntoTheFaeCourt(), "{3}{U}{U}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isOne();
    }
}
