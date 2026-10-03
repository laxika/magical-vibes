package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeornsHospitality.class, Forest.class, GrizzlyBears.class})
class BeornsHospitalityTest extends BaseCardTest {

    @Test
    void landfallPutsACounterOnTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new BeornsHospitality());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void activationMakesItABearWithPowerAndToughnessEqualToControlledLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent hospitality = harness.addToBattlefieldAndReturn(player1, new BeornsHospitality());
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hospitality)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, hospitality)).contains(CardSubtype.BEAR);
        assertThat(gqs.getEffectivePower(gd, hospitality)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hospitality)).isEqualTo(2);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, hospitality)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hospitality)).isEqualTo(3);
    }

    @Test
    void animatedHospitalityCanTargetItselfWithLandfallAndCountersIncreaseItsSize() {
        harness.addToBattlefield(player1, new Forest());
        Permanent hospitality = harness.addToBattlefieldAndReturn(player1, new BeornsHospitality());
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(hospitality.getId());
        harness.handlePermanentChosen(player1, hospitality.getId());
        harness.passBothPriorities();

        assertThat(hospitality.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, hospitality)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hospitality)).isEqualTo(3);
    }

    @Test
    void opposingLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new BeornsHospitality());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void landfallOffersOnlyCreaturesItsControllerControls() {
        Permanent hospitality = harness.addToBattlefieldAndReturn(player1, new BeornsHospitality());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(ownBear.getId());
        harness.handlePermanentChosen(player1, ownBear.getId());
        harness.passBothPriorities();

        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(hospitality.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void animationSurvivesTurnCleanupAndCountsOnlyItsControllersLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Permanent hospitality = harness.addToBattlefieldAndReturn(player1, new BeornsHospitality());
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.UNTAP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hospitality);
        assertThat(gqs.isCreature(gd, hospitality)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, hospitality)).contains(CardSubtype.BEAR);
        assertThat(gqs.getEffectivePower(gd, hospitality)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hospitality)).isEqualTo(1);
    }

    @Test
    void animationWithNoLandsPutsHospitalityIntoTheGraveyard() {
        harness.addToBattlefield(player1, new BeornsHospitality());
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Beorn's Hospitality");
        harness.assertInGraveyard(player1, "Beorn's Hospitality");
    }
}
