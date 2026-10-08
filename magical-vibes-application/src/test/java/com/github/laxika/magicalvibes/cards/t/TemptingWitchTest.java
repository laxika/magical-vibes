package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.y.YgraEaterOfAll;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemptingWitch.class, YgraEaterOfAll.class})
class TemptingWitchTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Food artifact token")
    void entersWithFoodToken() {
        castTemptingWitch();

        Permanent food = findPermanent(player1, "Food");
        assertThat(food.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(food.getCard().getSubtypes()).contains(CardSubtype.FOOD);
        assertThat(food.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Food makes target player lose 3 life")
    void sacrificingFoodMakesTargetPlayerLoseLife() {
        castTemptingWitch();
        findPermanent(player1, "Tempting Witch").setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void foodCanBeSacrificedForLifeImmediately() {
        castTemptingWitch();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        assertThat(findPermanent(player1, "Tempting Witch").isTapped()).isFalse();
    }

    @Test
    void witchCanTargetItsControllerAndSacrificeTappedFood() {
        castTemptingWitch();
        Permanent witch = findPermanent(player1, "Tempting Witch");
        witch.setSummoningSick(false);
        findPermanent(player1, "Food").tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player1.getId());
        assertThat(witch.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void summoningSickWitchCannotActivateTapAbility() {
        castTemptingWitch();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(findPermanent(player1, "Tempting Witch").isTapped()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    void witchCannotActivateWithoutFood() {
        addCreatureReady(player1, new TemptingWitch());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, 20);
        assertThat(findPermanent(player1, "Tempting Witch").isTapped()).isFalse();
    }

    @Test
    @CardUsed({TemptingWitch.class, YgraEaterOfAll.class})
    void witchCanSacrificeItselfWhenYgraMakesItFood() {
        addCreatureReady(player1, new TemptingWitch());
        harness.addToBattlefield(player1, new YgraEaterOfAll());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Tempting Witch");
        harness.assertNotOnBattlefield(player1, "Tempting Witch");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    private void castTemptingWitch() {
        harness.castFromHand(player1, new TemptingWitch(), "{2}{B}");
        resolveAllTriggers();
    }
}
