package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.c.CityOfBrass;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HavenwoodBattleground;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaFlare.class, Forest.class, AdarkarWastes.class, CityOfBrass.class, HavenwoodBattleground.class, LlanowarElves.class})
class ManaFlareTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a land for mana adds one additional mana of the type it produced")
    void addsExtraManaForController() {
        harness.addToBattlefield(player1, new ManaFlare());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        // 1 from Forest + 1 additional from Mana Flare = 2
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Effect is symmetric — an opponent's land also produces the additional mana")
    void addsExtraManaForOpponent() {
        harness.addToBattlefield(player1, new ManaFlare());
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("An activated mana ability on a nonbasic land also gets the additional mana")
    void addsExtraManaForActivatedLandAbility() {
        harness.addToBattlefield(player1, new ManaFlare());
        harness.addToBattlefield(player1, new AdarkarWastes());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("The additional mana follows the type chosen for an any-color land ability")
    void addsExtraManaOfChosenTypeForAnyColorLandAbility() {
        harness.addToBattlefield(player1, new ManaFlare());
        harness.addToBattlefield(player1, new CityOfBrass());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Without Mana Flare a land produces only its normal mana")
    void noExtraWithoutManaFlare() {
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
    @Test
    @DisplayName("Multiple Mana Flares each add one mana immediately")
    void multipleCopiesEachAddOneMana() {
        harness.addToBattlefield(player1, new ManaFlare());
        harness.addToBattlefield(player2, new ManaFlare());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A sacrificed land producing two mana gets only one extra mana")
    void addsOnlyOneManaWhenLandProducesTwoAndIsSacrificed() {
        harness.addToBattlefield(player1, new ManaFlare());
        harness.enterBattlefieldAndReturn(player1, new HavenwoodBattleground()).untap();

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Havenwood Battleground");
        harness.assertNotOnBattlefield(player1, "Havenwood Battleground");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Nonland mana sources do not receive additional mana")
    void doesNotAddManaForCreatureManaAbility() {
        harness.addToBattlefield(player1, new ManaFlare());
        addCreatureReady(player1, new LlanowarElves());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Extra colored mana from a pain land does not cause additional damage")
    void extraManaDoesNotRepeatPainLandDamage() {
        harness.addToBattlefield(player1, new ManaFlare());
        harness.addToBattlefield(player1, new AdarkarWastes());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        harness.assertLife(player1, 19);
        assertThat(gd.stack).isEmpty();
    }
}
