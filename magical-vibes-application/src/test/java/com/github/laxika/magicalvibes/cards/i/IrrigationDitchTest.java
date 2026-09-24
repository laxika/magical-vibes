package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(IrrigationDitch.class)
class IrrigationDitchTest extends BaseCardTest {

    @Test
    @DisplayName("Irrigation Ditch enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new IrrigationDitch()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent ditch = findPermanent(player1, "Irrigation Ditch");
        assertThat(ditch.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        harness.addToBattlefield(player1, new IrrigationDitch());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing Irrigation Ditch produces one green and one blue")
    void sacrificingProducesGreenAndBlueMana() {
        harness.addToBattlefield(player1, new IrrigationDitch());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Irrigation Ditch");
    }

}
