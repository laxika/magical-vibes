package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BloodchiefAscension;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        PerniciousDeed.class,
        BloodchiefAscension.class,
        Forest.class,
        GrizzlyBears.class,
        Millstone.class,
        Ornithopter.class,
        SerraAngel.class,
        PhyrexianArena.class
})
class PerniciousDeedTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys artifacts, creatures, and enchantments with mana value X or less on both battlefields")
    void destroysMatchingPermanentsUpToX() {
        addDeed(2);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BloodchiefAscension());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player1, new PhyrexianArena());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new PhyrexianArena());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Serra Angel", "Phyrexian Arena", "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Phyrexian Arena", "Forest");
        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Millstone");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Bloodchief Ascension");
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("X=0 destroys only zero-mana-value matching permanents")
    void zeroOnlyDestroysZeroManaValue() {
        addDeed(0);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player1, "Millstone");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Pernicious Deed is sacrificed as an activation cost")
    void sacrificesAsCost() {
        Permanent deed = addDeed(2);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(deed);
        harness.assertInGraveyard(player1, "Pernicious Deed");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addDeed(int xValue) {
        Permanent deed = harness.addToBattlefieldAndReturn(player1, new PerniciousDeed());
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return deed;
    }
}
