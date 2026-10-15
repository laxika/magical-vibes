package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinVoid;
import com.github.laxika.magicalvibes.cards.c.CelestialDawn;
import com.github.laxika.magicalvibes.cards.p.Pendelhaven;
import com.github.laxika.magicalvibes.cards.s.ScarwoodTreefolk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GauntletOfPower.class, Forest.class, Mountain.class, ZhalfirinVoid.class,
        ScarwoodTreefolk.class, GoblinSkycutter.class, CelestialDawn.class, Pendelhaven.class})
class GauntletOfPowerTest extends BaseCardTest {

    @Test
    void resolvingAwaitsColorChoice() {
        harness.setHand(player1, List.of(new GauntletOfPower()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    void chosenColorBoostsMatchingCreaturesForBothPlayers() {
        Permanent ownGreen = harness.addToBattlefieldAndReturn(player1, new ScarwoodTreefolk());
        Permanent ownRed = harness.addToBattlefieldAndReturn(player1, new GoblinSkycutter());
        Permanent opponentGreen = harness.addToBattlefieldAndReturn(player2, new ScarwoodTreefolk());
        addChosenGauntlet(CardColor.GREEN);

        assertThat(gqs.getEffectivePower(gd, ownGreen)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownGreen)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, ownRed)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownRed)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentGreen)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentGreen)).isEqualTo(6);
    }

    @Test
    void matchingBasicLandAddsManaForAnyPlayer() {
        addChosenGauntlet(CardColor.GREEN);
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void nonbasicLandDoesNotAddExtraMana() {
        addChosenGauntlet(CardColor.GREEN);
        harness.addToBattlefield(player1, new ZhalfirinVoid());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void basicLandOfAnotherColorDoesNotAddExtraMana() {
        addChosenGauntlet(CardColor.GREEN);
        harness.addToBattlefield(player2, new Mountain());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    private void addChosenGauntlet(CardColor color) {
        Permanent gauntlet = harness.addToBattlefieldAndReturn(player1, new GauntletOfPower());
        gauntlet.setChosenColor(color);
    }

    @Test
    void choosingColorOnResolutionEnablesManaBonus() {
        harness.setHand(player1, List.of(new GauntletOfPower()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonbasicLandProducingChosenColorDoesNotReceiveBonus() {
        addChosenGauntlet(CardColor.GREEN);
        harness.addToBattlefield(player1, new Pendelhaven());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void changedBasicLandManaTriggersForActualColorProduced() {
        addChosenGauntlet(CardColor.WHITE);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new CelestialDawn());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void changedBasicLandManaDoesNotTriggerForPrintedColor() {
        addChosenGauntlet(CardColor.GREEN);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new CelestialDawn());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void multipleGauntletsEachAddOneManaAndBoostCreatures() {
        addChosenGauntlet(CardColor.GREEN);
        addChosenGauntlet(CardColor.GREEN);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScarwoodTreefolk());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 3);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }
}
