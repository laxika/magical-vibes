package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlassOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts multicolored creatures you control")
    void boostsOwnMulticoloredCreatures() {
        harness.addToBattlefield(player1, new GlassOfTheGuildpact());
        harness.addToBattlefield(player1, new WoollyThoctar());

        Permanent woollyThoctar = findPermanent(player1, "Woolly Thoctar");

        assertThat(gqs.getEffectivePower(gd, woollyThoctar)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, woollyThoctar)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not boost monocolored or opponent creatures")
    void doesNotBoostMonocoloredOrOpponentCreatures() {
        harness.addToBattlefield(player1, new GlassOfTheGuildpact());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new QasaliAmbusher());

        Permanent grizzlyBears = findPermanent(player1, "Grizzly Bears");
        Permanent qasaliAmbusher = findPermanent(player2, "Qasali Ambusher");

        assertThat(gqs.getEffectivePower(gd, grizzlyBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, grizzlyBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, qasaliAmbusher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, qasaliAmbusher)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus disappears when Glass of the Guildpact leaves the battlefield")
    void bonusDisappearsWhenGlassLeavesBattlefield() {
        harness.addToBattlefield(player1, new GlassOfTheGuildpact());
        harness.addToBattlefield(player1, new WoollyThoctar());

        Permanent woollyThoctar = findPermanent(player1, "Woolly Thoctar");
        assertThat(gqs.getEffectivePower(gd, woollyThoctar)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Glass of the Guildpact"));

        assertThat(gqs.getEffectivePower(gd, woollyThoctar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, woollyThoctar)).isEqualTo(4);
    }
}
