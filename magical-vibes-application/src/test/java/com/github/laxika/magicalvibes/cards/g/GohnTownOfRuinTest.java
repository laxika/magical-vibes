package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GohnTownOfRuin.class)
class GohnTownOfRuinTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new GohnTownOfRuin()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void addsBlackManaWhenChosen() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void addsGreenManaWhenChosen() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    private Permanent addReadyLand() {
        Permanent land = new Permanent(new GohnTownOfRuin());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(land);
        return land;
    }
}
