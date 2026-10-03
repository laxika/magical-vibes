package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImperialRecoveryUnit;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CidFreeflierPilot.class, LeoninScimitar.class, ImperialRecoveryUnit.class, GrizzlyBears.class})
class CidFreeflierPilotTest extends BaseCardTest {

    @Test
    void equipmentAndVehicleSpellsCostOneLess() {
        harness.addToBattlefield(player1, new CidFreeflierPilot());

        harness.castFromHand(player1, new LeoninScimitar(), "");
        harness.passBothPriorities();

        harness.castFromHand(player1, new ImperialRecoveryUnit(), "{1}{W}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void hasFlyingOnlyDuringItsControllersTurn() {
        Permanent cid = harness.addToBattlefieldAndReturn(player1, new CidFreeflierPilot());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, cid, Keyword.FLYING)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, cid, Keyword.FLYING)).isFalse();
    }

    @Test
    void returnsTargetEquipmentOrVehicleFromGraveyardToHand() {
        addCreatureReady(player1, new CidFreeflierPilot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card equipment = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(equipment));

        harness.activateAbility(player1, 0, 0, null, equipment.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertNotInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    void cannotTargetOtherCardTypesInGraveyard() {
        addCreatureReady(player1, new CidFreeflierPilot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsVehicleAndPaysManaAndTapCosts() {
        Permanent cid = addCreatureReady(player1, new CidFreeflierPilot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card vehicle = new ImperialRecoveryUnit();
        harness.setGraveyard(player1, List.of(vehicle));

        harness.activateAbility(player1, 0, 0, null, vehicle.getId(), Zone.GRAVEYARD);

        assertThat(cid.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Imperial Recovery Unit");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Imperial Recovery Unit");
        harness.assertNotInGraveyard(player1, "Imperial Recovery Unit");
    }

    @Test
    void cannotTargetOpponentsEquipment() {
        addCreatureReady(player1, new CidFreeflierPilot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card equipment = new LeoninScimitar();
        harness.setGraveyard(player2, List.of(equipment));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, equipment.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new CidFreeflierPilot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card equipment = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(equipment));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, equipment.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutTwoMana() {
        addCreatureReady(player1, new CidFreeflierPilot());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Card equipment = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(equipment));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, equipment.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReturnTargetThatLeavesGraveyardBeforeResolution() {
        addCreatureReady(player1, new CidFreeflierPilot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card equipment = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(equipment));
        harness.activateAbility(player1, 0, 0, null, equipment.getId(), Zone.GRAVEYARD);

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Leonin Scimitar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReduceOpponentsEquipmentSpells() {
        harness.addToBattlefield(player2, new CidFreeflierPilot());
        harness.setHand(player1, List.of(new LeoninScimitar()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceNonEquipmentNonVehicleSpells() {
        harness.addToBattlefield(player1, new CidFreeflierPilot());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceColoredManaRequirement() {
        harness.addToBattlefield(player1, new CidFreeflierPilot());
        harness.setHand(player1, List.of(new ImperialRecoveryUnit()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
