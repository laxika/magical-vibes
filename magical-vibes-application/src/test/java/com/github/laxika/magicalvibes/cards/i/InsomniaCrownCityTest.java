package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(InsomniaCrownCity.class)
class InsomniaCrownCityTest extends BaseCardTest {

    @Test
    @DisplayName("Insomnia, Crown City enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new InsomniaCrownCity()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Insomnia, Crown City").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping produces one white mana")
    void tappingProducesWhiteMana() {
        Permanent land = addLandReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping produces one black mana")
    void tappingProducesBlackMana() {
        Permanent land = addLandReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Insomnia enters tapped when put directly onto the battlefield")
    void entersTappedWithoutBeingPlayed() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new InsomniaCrownCity());

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An already tapped Insomnia cannot produce mana")
    void cannotActivateWhileTapped() {
        Permanent land = addLandReady(player1);
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An untapped Insomnia can produce mana on the turn it enters")
    void canActivateOnTurnItEnters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new InsomniaCrownCity());
        land.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addLandReady(Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new InsomniaCrownCity());
        land.setSummoningSick(false);
        return land;
    }
}
