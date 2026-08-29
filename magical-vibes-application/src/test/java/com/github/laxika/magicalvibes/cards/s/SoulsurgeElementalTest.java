package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SoulsurgeElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures its controller controls")
    void powerEqualsControlledCreatures() {
        Permanent elemental = addElementalReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power updates as creatures enter and leave its controller's battlefield")
    void powerUpdatesDynamically() {
        Permanent elemental = addElementalReady(player1);

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);

        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
    }

    private Permanent addElementalReady(Player player) {
        Permanent permanent = new Permanent(new SoulsurgeElemental());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
