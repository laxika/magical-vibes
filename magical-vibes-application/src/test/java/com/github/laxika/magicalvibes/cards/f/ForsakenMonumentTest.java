package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.s.SkyclaveSentinel;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForsakenMonument.class, Forest.class, GrizzlyBears.class, SkyclaveSentinel.class,
        SolRing.class, Wastes.class, EnsoulArtifact.class})
class ForsakenMonumentTest extends BaseCardTest {

    @Test
    void boostsOnlyColorlessCreatures() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        Permanent colorlessCreature = harness.addToBattlefieldAndReturn(player1,
                new SkyclaveSentinel());
        Permanent coloredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, colorlessCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, colorlessCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, coloredCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, coloredCreature)).isEqualTo(2);
    }

    @Test
    void tappingWastesAddsOneAdditionalColorlessMana() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        harness.addToBattlefield(player1, new Wastes());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void tappingSolRingAddsOnlyOneAdditionalMana() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        harness.addToBattlefield(player1, new SolRing());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    void colorlessSpellGainsTwoLife() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        harness.setHand(player1, List.of(new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void coloredSpellDoesNotGainLife() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void animatedMonumentReceivesItsOwnBoost() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new ForsakenMonument());
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, monument.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, monument)).isTrue();
        assertThat(gqs.getEffectivePower(gd, monument)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, monument)).isEqualTo(7);
    }

    @Test
    void doesNotBoostOpponentsColorlessCreatures() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SkyclaveSentinel());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void tappingForestDoesNotAddColorlessMana() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void opponentsLandDoesNotReceiveAdditionalMana() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        harness.addToBattlefield(player2, new Wastes());
        harness.forceActivePlayer(player2);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void opponentsSolRingDoesNotReceiveAdditionalMana() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        harness.addToBattlefield(player2, new SolRing());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void eachTappedPermanentAddsAdditionalManaImmediately() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        harness.addToBattlefield(player1, new Wastes());
        harness.addToBattlefield(player1, new SolRing());

        harness.tapPermanent(player1, 1);
        harness.activateAbility(player1, 2, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsColorlessSpellDoesNotGainLife() {
        harness.addToBattlefield(player1, new ForsakenMonument());
        harness.setHand(player2, List.of(new SolRing()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentsLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        harness.assertLife(player2, opponentsLifeBefore);
    }
}
