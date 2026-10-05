package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BenevolentUnicorn;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.g.GibberingHyenas;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.p.PutridLeech;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaerveksHex.class, GibberingHyenas.class, GiantMantis.class, IronTuskElephant.class,
        FeralShadow.class, PutridLeech.class, Forest.class, BenevolentUnicorn.class})
class KaerveksHexTest extends BaseCardTest {

    private void castHex() {
        harness.castFromHand(player1, new KaerveksHex(), "{3}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Green nonblack creatures take 2 damage")
    void greenCreaturesTakeTwoDamage() {
        harness.addToBattlefield(player1, new GibberingHyenas());
        Permanent mantis = harness.addToBattlefieldAndReturn(player2, new GiantMantis());

        castHex();

        harness.assertNotOnBattlefield(player1, "Gibbering Hyenas");
        assertThat(mantis.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonblack, nongreen creatures take only 1 damage")
    void nonblackNongreenCreaturesTakeOneDamage() {
        Permanent elephant = harness.addToBattlefieldAndReturn(player2, new IronTuskElephant());

        castHex();

        assertThat(elephant.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Black creatures are untouched")
    void blackCreaturesAreUntouched() {
        Permanent shadow = harness.addToBattlefieldAndReturn(player2, new FeralShadow());

        castHex();

        assertThat(shadow.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A black-green creature takes only the additional 1 damage")
    void blackGreenCreatureTakesOneDamage() {
        Permanent leech = harness.addToBattlefieldAndReturn(player2, new PutridLeech());

        castHex();

        assertThat(leech.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Noncreature permanents are not damaged")
    void doesNotDamageNoncreaturePermanents() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        castHex();

        assertThat(forest.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Benevolent Unicorn reduces the combined damage to green creatures only once")
    void spellDamageReductionAppliesOnceToCombinedDamage() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new BenevolentUnicorn());
        Permanent mantis = harness.addToBattlefieldAndReturn(player2, new GiantMantis());
        Permanent hyenas = harness.addToBattlefieldAndReturn(player1, new GibberingHyenas());

        castHex();

        assertThat(mantis.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Gibbering Hyenas");
        assertThat(hyenas.getMarkedDamage()).isEqualTo(1);
        assertThat(unicorn.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Resolves on an empty battlefield without damaging either player")
    void emptyBattlefieldDoesNotDamagePlayers() {
        int firstLife = gd.playerLifeTotals.get(player1.getId());
        int secondLife = gd.playerLifeTotals.get(player2.getId());

        castHex();

        harness.assertLife(player1, firstLife);
        harness.assertLife(player2, secondLife);
        harness.assertInGraveyard(player1, "Kaervek's Hex");
        assertThat(gd.stack).isEmpty();
    }
}
