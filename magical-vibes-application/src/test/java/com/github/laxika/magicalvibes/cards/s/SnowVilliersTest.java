package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SnowVilliers.class, GrizzlyBears.class})
class SnowVilliersTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures you control and toughness stays 3")
    void powerEqualsControlledCreatures() {
        Permanent snow = addSnow(player1);

        assertThat(gqs.getEffectivePower(gd, snow)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, snow)).isEqualTo(3);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, snow)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, snow)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power counts only creatures controlled by Snow Villiers's controller")
    void powerIgnoresOpponentsCreatures() {
        Permanent snow = addSnow(player1);

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, snow)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power updates when creatures leave the battlefield")
    void powerUpdatesWhenCreaturesChange() {
        Permanent snow = addSnow(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, snow)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getCard().getName().equals("Grizzly Bears"));

        assertThat(gqs.getEffectivePower(gd, snow)).isEqualTo(1);
    }

    private Permanent addSnow(Player player) {
        Permanent permanent = new Permanent(new SnowVilliers());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
