package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(SweettoothWitch.class)
class SweettoothWitchTest extends BaseCardTest {

    @Test
    void entersAndCreatesFoodToken() {
        harness.castFromHand(player1, new SweettoothWitch(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    void sacrificesFoodToMakeTargetPlayerLoseTwoLife() {
        harness.castFromHand(player1, new SweettoothWitch(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void foodCanBeSacrificedForThreeLifeImmediatelyAfterCreation() {
        harness.castFromHand(player1, new SweettoothWitch(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Sweettooth Witch");
    }

    @Test
    void canTargetItsControllerWhileWitchAndFoodAreTapped() {
        harness.castFromHand(player1, new SweettoothWitch(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        findPermanent(player1, "Sweettooth Witch").tap();
        findPermanent(player1, "Food").tap();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Food")).isZero();
        harness.assertOnBattlefield(player1, "Sweettooth Witch");
    }

    @Test
    void cannotActivateWithoutFood() {
        harness.addToBattlefield(player1, new SweettoothWitch());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Sweettooth Witch");
    }

    @Test
    void cannotSacrificeOpponentsFood() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new SweettoothWitch(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new SweettoothWitch());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player2, "Food")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }
}
