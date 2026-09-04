package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Badlands;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaFlare.class, Forest.class, Badlands.class})
class ManaFlareTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a land for mana adds one additional mana of the type it produced")
    void addsExtraManaForController() {
        harness.addToBattlefield(player1, new ManaFlare());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        // 1 from Forest + 1 additional from Mana Flare = 2
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Effect is symmetric — an opponent's land also produces the additional mana")
    void addsExtraManaForOpponent() {
        harness.addToBattlefield(player1, new ManaFlare());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Without Mana Flare a land produces only its normal mana")
    void noExtraWithoutManaFlare() {
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void addsExtraManaForActivatedDualLandAbility() {
        harness.addToBattlefield(player1, new ManaFlare());
        harness.addToBattlefield(player1, new Badlands());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }
}
