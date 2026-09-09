package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonicaRambeau.class, GrizzlyBears.class, Shock.class})
class MonicaRambeauTest extends BaseCardTest {

    @Test
    void transformsByPayingItsAbilityCostAtSorcerySpeed() {
        Permanent monica = addMonica(player1, false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(monica.isTransformed()).isTrue();
        assertThat(monica.getCard()).isInstanceOf(PhotonLivingLight.class);
    }

    @Test
    void frontFaceProwessBoostsItForANoncreatureSpell() {
        Permanent monica = addMonica(player1, false);
        int powerBefore = gqs.getEffectivePower(gd, monica);
        int toughnessBefore = gqs.getEffectiveToughness(gd, monica);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, monica)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, monica)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    void backFaceProwessAndCounterTriggerAffectTheCorrectCreatures() {
        Permanent monica = addMonica(player1, true);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        int photonPowerBefore = gqs.getEffectivePower(gd, monica);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, monica)).isEqualTo(photonPowerBefore + 1);
        assertThat(bear.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(monica.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    void backFaceAbilitiesDoNotTriggerForCreatureSpells() {
        Permanent monica = addMonica(player1, true);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        int photonPowerBefore = gqs.getEffectivePower(gd, monica);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, monica)).isEqualTo(photonPowerBefore);
        assertThat(bear.getPlusOnePlusOneCounters()).isZero();
        assertThat(monica.getPlusOnePlusOneCounters()).isZero();
    }

    private Permanent addMonica(Player player, boolean transformed) {
        MonicaRambeau card = new MonicaRambeau();
        Permanent permanent = addCreatureReady(player, card);
        if (transformed) {
            permanent.setTransformed(true);
            permanent.setCard(card.getBackFaceCard());
        }
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

}
