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

    private Permanent addLandReady(Player player) {
        Permanent land = new Permanent(new InsomniaCrownCity());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(land);
        return land;
    }
}
