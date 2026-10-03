package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CentaurNurturer.class})
class CentaurNurturerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains 3 life")
    void entersAndGainsThreeLife() {
        harness.setLife(player1, 17);
        harness.castFromHand(player1, new CentaurNurturer(), "{3}{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Tap ability adds mana of the chosen color")
    void tapsForAnyColor() {
        Permanent nurturer = addCreatureReady(player1, new CentaurNurturer());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(nurturer.isTapped()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLACK", "RED", "GREEN"})
    void tapsForEachOtherColorWithoutUsingTheStack(ManaColor color) {
        Permanent nurturer = addCreatureReady(player1, new CentaurNurturer());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(nurturer.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.castFromHand(player1, new CentaurNurturer(), "{3}{G}");
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(findPermanent(player1, "Centaur Nurturer").isTapped()).isFalse();
    }

    @Test
    void cannotActivateAgainWithoutUntapping() {
        addCreatureReady(player1, new CentaurNurturer());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void lifeGainWaitsForTheEnterTriggerAndOnlyBenefitsItsController() {
        harness.setLife(player1, 17);
        harness.setLife(player2, 12);
        harness.castFromHand(player1, new CentaurNurturer(), "{3}{G}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Centaur Nurturer");
        harness.assertLife(player1, 17);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 12);
    }
}
