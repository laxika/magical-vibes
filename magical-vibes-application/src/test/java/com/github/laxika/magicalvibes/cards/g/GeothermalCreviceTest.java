package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed(GeothermalCrevice.class)
class GeothermalCreviceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new GeothermalCrevice()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Geothermal Crevice").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one red mana")
    void tapAddsOneRedMana() {
        Permanent crevice = harness.addToBattlefieldAndReturn(player1, new GeothermalCrevice());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(crevice.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Geothermal Crevice");
    }

    @Test
    @DisplayName("Tap and sacrifice adds one black and one green mana and moves the land to the graveyard")
    void sacrificeAddsBlackAndGreenMana() {
        harness.addToBattlefield(player1, new GeothermalCrevice());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Geothermal Crevice");
        harness.assertInGraveyard(player1, "Geothermal Crevice");
    }

    @Test
    @DisplayName("Cannot activate the sacrifice ability after the land has been tapped")
    void cannotActivateSacrificeAbilityAfterTapping() {
        Permanent crevice = harness.addToBattlefieldAndReturn(player1, new GeothermalCrevice());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(crevice.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertOnBattlefield(player1, "Geothermal Crevice");
    }
}
