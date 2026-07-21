package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.s.SproutingThrinax;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KnightOfNewAlaraTest extends BaseCardTest {

    private Permanent creature(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Two-color creature gets +2/+2")
    void twoColorCreature() {
        harness.addToBattlefield(player1, new KnightOfNewAlara());
        harness.addToBattlefield(player1, new QasaliAmbusher()); // {1}{G}{W}, 2/3, GW

        Permanent ambusher = creature(player1, "Qasali Ambusher");
        assertThat(gqs.getEffectivePower(gd, ambusher)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ambusher)).isEqualTo(5);
    }

    @Test
    @DisplayName("Three-color creature gets +3/+3")
    void threeColorCreature() {
        harness.addToBattlefield(player1, new KnightOfNewAlara());
        harness.addToBattlefield(player1, new WoollyThoctar()); // {R}{G}{W}, 5/4, RGW

        Permanent thoctar = creature(player1, "Woolly Thoctar");
        assertThat(gqs.getEffectivePower(gd, thoctar)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, thoctar)).isEqualTo(7);
    }

    @Test
    @DisplayName("Monocolored creature gets no bonus")
    void monocoloredCreature() {
        harness.addToBattlefield(player1, new KnightOfNewAlara());
        harness.addToBattlefield(player1, new GrizzlyBears()); // {1}{G}, 2/2, one color

        Permanent bears = creature(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost itself even though it is multicolored")
    void doesNotBoostItself() {
        harness.addToBattlefield(player1, new KnightOfNewAlara()); // {2}{G}{W}, 2/2, GW

        Permanent knight = creature(player1, "Knight of New Alara");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not affect an opponent's multicolored creature")
    void onlyOwnCreatures() {
        harness.addToBattlefield(player1, new KnightOfNewAlara());
        harness.addToBattlefield(player2, new SproutingThrinax()); // {B}{R}{G}, 3/3, BRG

        Permanent thrinax = creature(player2, "Sprouting Thrinax");
        assertThat(gqs.getEffectivePower(gd, thrinax)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thrinax)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus is removed when Knight of New Alara leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new KnightOfNewAlara());
        harness.addToBattlefield(player1, new WoollyThoctar());

        Permanent thoctar = creature(player1, "Woolly Thoctar");
        assertThat(gqs.getEffectivePower(gd, thoctar)).isEqualTo(8);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Knight of New Alara"));

        assertThat(gqs.getEffectivePower(gd, thoctar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thoctar)).isEqualTo(4);
    }
}
