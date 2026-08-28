package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KarametraGodOfHarvests;
import com.github.laxika.magicalvibes.cards.n.NyleaGodOfTheHunt;
import com.github.laxika.magicalvibes.cards.n.NyleasDisciple;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AltarOfThePantheon.class, NyleasDisciple.class, KarametraGodOfHarvests.class,
        GrizzlyBears.class, GlorySeeker.class, NyleaGodOfTheHunt.class})
class AltarOfThePantheonTest extends BaseCardTest {

    @Test
    void increasesGreenDevotionUsedByAnEtbAbility() {
        harness.addToBattlefield(player1, new AltarOfThePantheon());
        harness.setHand(player1, List.of(new NyleasDisciple()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
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
}
