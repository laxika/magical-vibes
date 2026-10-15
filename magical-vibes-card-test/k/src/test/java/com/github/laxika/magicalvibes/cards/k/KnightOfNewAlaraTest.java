package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.cards.s.SproutingThrinax;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightOfNewAlara.class, QasaliAmbusher.class, WoollyThoctar.class,
        GrizzlyBears.class, SproutingThrinax.class})
class KnightOfNewAlaraTest extends BaseCardTest {

    @Test
    @DisplayName("Two-color creature gets +2/+2")
    void twoColorCreature() {
        harness.addToBattlefield(player1, new KnightOfNewAlara());
        Permanent ambusher = harness.addToBattlefieldAndReturn(player1, new QasaliAmbusher());
        assertThat(gqs.getEffectivePower(gd, ambusher)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ambusher)).isEqualTo(5);
    }

    @Test
    @DisplayName("Three-color creature gets +3/+3")
    void threeColorCreature() {
        harness.addToBattlefield(player1, new KnightOfNewAlara());
        Permanent thoctar = harness.addToBattlefieldAndReturn(player1, new WoollyThoctar());
        assertThat(gqs.getEffectivePower(gd, thoctar)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, thoctar)).isEqualTo(7);
    }

    @Test
    @DisplayName("Monocolored creature gets no bonus")
    void monocoloredCreature() {
        harness.addToBattlefield(player1, new KnightOfNewAlara());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost itself even though it is multicolored")
    void doesNotBoostItself() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfNewAlara());
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not affect an opponent's multicolored creature")
    void onlyOwnCreatures() {
        harness.addToBattlefield(player1, new KnightOfNewAlara());
        Permanent thrinax = harness.addToBattlefieldAndReturn(player2, new SproutingThrinax());
        assertThat(gqs.getEffectivePower(gd, thrinax)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, thrinax)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus is removed when Knight of New Alara leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new KnightOfNewAlara());
        Permanent thoctar = harness.addToBattlefieldAndReturn(player1, new WoollyThoctar());
        assertThat(gqs.getEffectivePower(gd, thoctar)).isEqualTo(8);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Knight of New Alara"));

        assertThat(gqs.getEffectivePower(gd, thoctar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thoctar)).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple Knights boost each other and their bonuses stack")
    void multipleKnightsStack() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KnightOfNewAlara());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KnightOfNewAlara());
        Permanent ambusher = harness.addToBattlefieldAndReturn(player1, new QasaliAmbusher());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ambusher)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ambusher)).isEqualTo(7);
    }

    @Test
    @DisplayName("Knight immediately boosts creatures already on the battlefield")
    void boostsExistingCreatures() {
        Permanent thoctar = harness.addToBattlefieldAndReturn(player1, new WoollyThoctar());
        assertThat(gqs.getEffectivePower(gd, thoctar)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thoctar)).isEqualTo(4);

        harness.enterBattlefieldAndReturn(player1, new KnightOfNewAlara());

        assertThat(gqs.getEffectivePower(gd, thoctar)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, thoctar)).isEqualTo(7);
    }
}
