package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RabanastreRoyalCity.class)
class RabanastreRoyalCityTest extends BaseCardTest {

    @Test
    @DisplayName("Rabanastre, Royal City enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new RabanastreRoyalCity()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Rabanastre, Royal City").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping produces one red mana")
    void tappingProducesRedMana() {
        Permanent land = addLandReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping produces one white mana")
    void tappingProducesWhiteMana() {
        Permanent land = addLandReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    private Permanent addLandReady(Player player) {
        Permanent land = new Permanent(new RabanastreRoyalCity());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(land);
        return land;
    }
}
