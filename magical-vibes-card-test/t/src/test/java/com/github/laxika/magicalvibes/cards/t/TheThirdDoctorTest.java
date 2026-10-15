package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AdiposeOffspring;
import com.github.laxika.magicalvibes.cards.m.MarthaJones;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheThirdDoctor.class, AdiposeOffspring.class, MarthaJones.class})
class TheThirdDoctorTest extends BaseCardTest {

    private static final String CLUE_MODE = "Create a Clue token";
    private static final String FOOD_MODE = "Create a Food token";
    private static final String TREASURE_MODE = "Create a Treasure token";

    @Test
    void createsAClueToken() {
        castDoctor(CLUE_MODE);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void createsAFoodToken() {
        castDoctor(FOOD_MODE);

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void createsATreasureToken() {
        castDoctor(TREASURE_MODE);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    @Test
    void getsPlusOnePlusOneForEachNoncreatureTokenYouControl() {
        Permanent doctor = castDoctor(TREASURE_MODE);
        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(3);

        harness.enterBattlefieldAndReturn(player1, new MarthaJones());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(4);

        harness.enterBattlefieldAndReturn(player1, new AdiposeOffspring());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(4);

        harness.enterBattlefieldAndReturn(player2, new MarthaJones());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(4);
    }

    @Test
    void choosesTokenOnlyWhenEnterAbilityResolves() {
        harness.castFromHand(player1, new TheThirdDoctor(), "{2}{G}{U}");
        harness.passBothPriorities();

        Permanent doctor = findPermanent(player1, "The Third Doctor");
        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(2);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.handleListChoice(player1, FOOD_MODE);

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(3);
    }

    @Test
    void canChooseTreasureWhenEnteringWithoutBeingCast() {
        harness.enterBattlefieldAndReturn(player1, new TheThirdDoctor());
        harness.passBothPriorities();
        harness.handleListChoice(player1, TREASURE_MODE);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    @Test
    void losesBoostAsSoonAsFoodIsSacrificed() {
        Permanent doctor = castDoctor(FOOD_MODE);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int foodIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Food"));

        harness.activateAbility(player1, foodIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
    }

    private Permanent castDoctor(String mode) {
        harness.castFromHand(player1, new TheThirdDoctor(), "{2}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);

        return findPermanent(player1, "The Third Doctor");
    }
}
