package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BowOfNylea;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KarametraGodOfHarvests;
import com.github.laxika.magicalvibes.cards.n.NyleaGodOfTheHunt;
import com.github.laxika.magicalvibes.cards.n.NyleasDisciple;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheSun;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AltarOfThePantheon.class, NyleasDisciple.class, KarametraGodOfHarvests.class,
        GrizzlyBears.class, GlorySeeker.class, NyleaGodOfTheHunt.class,
        AnaxHardenedInTheForge.class, BowOfNylea.class, OmenOfTheSun.class})
class AltarOfThePantheonTest extends BaseCardTest {

    @Test
    void increasesGreenDevotionUsedByAnEtbAbility() {
        harness.addToBattlefield(player1, new AltarOfThePantheon());
        harness.setHand(player1, List.of(new NyleasDisciple()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    void increasesDevotionToAColorCombination() {
        harness.addToBattlefield(player1, new AltarOfThePantheon());
        var karametra = harness.addToBattlefieldAndReturn(player1, new KarametraGodOfHarvests());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GlorySeeker());

        assertThat(gqs.isCreature(gd, karametra)).isTrue();
    }

    @Test
    void manaAbilityGainsLifeWhenAGodIsControlled() {
        harness.addToBattlefield(player1, new AltarOfThePantheon());
        harness.addToBattlefield(player1, new NyleaGodOfTheHunt());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void manaAbilityDoesNotGainLifeWithoutAQualifyingPermanent() {
        harness.addToBattlefield(player1, new AltarOfThePantheon());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void multipleAltarsIncreaseDemigodPowerOnlyForTheirController() {
        var anax = harness.addToBattlefieldAndReturn(player1, new AnaxHardenedInTheForge());
        var opposingAnax = harness.addToBattlefieldAndReturn(player2, new AnaxHardenedInTheForge());
        assertThat(gqs.getEffectivePower(gd, anax)).isEqualTo(2);

        harness.addToBattlefield(player1, new AltarOfThePantheon());
        assertThat(gqs.getEffectivePower(gd, anax)).isEqualTo(3);

        harness.addToBattlefield(player1, new AltarOfThePantheon());
        assertThat(gqs.getEffectivePower(gd, anax)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingAnax)).isEqualTo(2);
    }

    @Test
    void colorCombinationGetsOnlyOneExtraDevotion() {
        harness.addToBattlefield(player1, new AltarOfThePantheon());
        var karametra = harness.addToBattlefieldAndReturn(player1, new KarametraGodOfHarvests());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.isCreature(gd, karametra)).isFalse();
    }

    @Test
    void manaAbilityGainsLifeWhenADemigodIsControlled() {
        var altar = harness.addToBattlefieldAndReturn(player1, new AltarOfThePantheon());
        harness.addToBattlefield(player1, new AnaxHardenedInTheForge());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(altar.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaAbilityGainsLifeForANoncreatureLegendaryEnchantment() {
        harness.addToBattlefield(player1, new AltarOfThePantheon());
        harness.addToBattlefield(player1, new BowOfNylea());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void multipleQualifyingPermanentsStillGainOnlyOneLife() {
        harness.addToBattlefield(player1, new AltarOfThePantheon());
        harness.addToBattlefield(player1, new AnaxHardenedInTheForge());
        harness.addToBattlefield(player1, new NyleaGodOfTheHunt());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    void ordinaryEnchantmentAndOpponentsDemigodDoNotQualify() {
        harness.addToBattlefield(player1, new AltarOfThePantheon());
        harness.addToBattlefield(player1, new OmenOfTheSun());
        harness.addToBattlefield(player2, new AnaxHardenedInTheForge());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
