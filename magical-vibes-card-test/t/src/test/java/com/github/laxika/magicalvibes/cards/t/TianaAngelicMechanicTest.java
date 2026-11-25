package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.b.BlinkOfAnEye;
import com.github.laxika.magicalvibes.cards.c.CabalEvangel;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.h.HelmOfTheHost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TianaAngelicMechanic.class, DuskLegionDreadnought.class, ArvadTheCursed.class,
        CabalEvangel.class, HelmOfTheHost.class, BlinkOfAnEye.class})
class TianaAngelicMechanicTest extends BaseCardTest {

    @Test
    @DisplayName("A Vehicle Tiana crews perpetually gets +1/+0")
    void tianaPerpetuallyBoostsVehicleSheCrews() {
        addCreatureReady(player1, new TianaAngelicMechanic());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());

        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
    }

    @Test
    @DisplayName("A Vehicle another legendary creature crews perpetually gets +1/+0")
    void anotherLegendaryCreaturePerpetuallyBoostsVehicleItCrews() {
        Permanent tiana = addCreatureReady(player1, new TianaAngelicMechanic());
        tiana.tap();
        addCreatureReady(player1, new ArvadTheCursed());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());

        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
    }

    @Test
    @DisplayName("A nonlegendary creature crewing doesn't trigger it")
    void nonLegendaryCreatureDoesNotTriggerTiana() {
        Permanent tiana = addCreatureReady(player1, new TianaAngelicMechanic());
        tiana.tap();
        addCreatureReady(player1, new CabalEvangel());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());

        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(4);
    }

    @Test
    @DisplayName("A nonlegendary copy of Tiana triggers when it crews")
    void nonlegendaryCopyOfTianaTriggersWhenItCrews() {
        Permanent tiana = addCreatureReady(player1, new TianaAngelicMechanic());
        Permanent helm = harness.addToBattlefieldAndReturn(player1, new HelmOfTheHost());
        helm.setAttachedTo(tiana.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();
        tiana.tap();
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());

        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        resolveAllTriggers();

        assertThat(token.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(6);
    }

    @Test
    @DisplayName("An opponent's legendary creature crewing doesn't trigger it")
    void opponentsLegendaryCreatureDoesNotTriggerTiana() {
        addCreatureReady(player1, new TianaAngelicMechanic());
        addCreatureReady(player2, new ArvadTheCursed());
        Permanent vehicle = addCreatureReady(player2, new DuskLegionDreadnought());

        harness.activateAbility(player2, indexOf(player2, vehicle), null, null);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(4);
    }

    @Test
    @DisplayName("The Vehicle is boosted even after Tiana leaves")
    void triggerStillBoostsVehicleAfterTianaLeaves() {
        Permanent tiana = addCreatureReady(player1, new TianaAngelicMechanic());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);

        harness.setHand(player2, List.of(new BlinkOfAnEye()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, tiana.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tiana, Angelic Mechanic");
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost stays on the Vehicle card after it returns to hand and stacks with another crew")
    void perpetualBoostSurvivesReturningToHandAndStacksWithAnotherCrew() {
        Permanent tiana = addCreatureReady(player1, new TianaAngelicMechanic());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);

        harness.setHand(player2, List.of(new BlinkOfAnEye()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, vehicle.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var returnedCard = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(vehicle.getCard().getId()))
                .findFirst().orElseThrow();
        harness.castFromHand(player1, returnedCard, "{5}");
        resolveAllTriggers();
        Permanent returned = findPermanent(player1, "Dusk Legion Dreadnought");
        tiana.untap();
        harness.activateAbility(player1, indexOf(player1, returned), null, null);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, returned)).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(6);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    @Test
    @DisplayName("A Vehicle that left the battlefield before the ability resolves isn't boosted (CR 400.7)")
    void vehicleThatLeftIsNotBoosted() {
        addCreatureReady(player1, new TianaAngelicMechanic());
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        harness.activateAbility(player1, indexOf(player1, vehicle), null, null);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, vehicle));
        resolveAllTriggers();

        assertThat(gd.perpetualCardPowerToughnessModifiers).doesNotContainKey(vehicle.getCard().getId());
    }
}
