package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TangledIslet.class)
class TangledIsletTest extends BaseCardTest {

    @Test
    @DisplayName("Tangled Islet enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new TangledIslet()));

        harness.playLand(player1, 0);

        Permanent islet = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(islet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tangled Islet adds green mana")
    void addsGreenMana() {
        Permanent islet = addReadyIslet(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(islet.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tangled Islet adds blue mana")
    void addsBlueMana() {
        Permanent islet = addReadyIslet(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(islet.isTapped()).isTrue();
    }

    private Permanent addReadyIslet(Player player) {
        Permanent permanent = new Permanent(new TangledIslet());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
