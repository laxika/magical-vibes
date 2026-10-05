package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HotshotMechanic;
import com.github.laxika.magicalvibes.cards.j.JukaiPreserver;
import com.github.laxika.magicalvibes.cards.p.ProdigysPrototype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MechHangar.class, HotshotMechanic.class, ProdigysPrototype.class, JukaiPreserver.class})
class MechHangarTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void addsColorlessMana() {
        Permanent hangar = addHangar(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(hangar.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second ability adds mana that can cast a Pilot spell")
    void restrictedManaCastsPilotSpell() {
        addHangar(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "WHITE");

        harness.setHand(player1, List.of(new HotshotMechanic()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The second ability adds mana that can cast a Vehicle spell")
    void restrictedManaCastsVehicleSpell() {
        addHangar(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new ProdigysPrototype()));
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The second ability's mana cannot cast another spell")
    void restrictedManaCannotCastOtherSpell() {
        addHangar(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new JukaiPreserver()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The third ability animates a target Vehicle until end of turn")
    void animatesTargetVehicleUntilEndOfTurn() {
        addHangar(player1);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ProdigysPrototype());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.isArtifact(gd, vehicle)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    @DisplayName("The third ability cannot target a non-Vehicle")
    void cannotTargetNonVehicle() {
        addHangar(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JukaiPreserver());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The third ability can animate an opponent's Vehicle without tapping it")
    void animatesOpponentsVehicle() {
        Permanent hangar = addHangar(player1);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new ProdigysPrototype());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, vehicle.getId());

        assertThat(hangar.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.isArtifact(gd, vehicle)).isTrue();
        assertThat(vehicle.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(vehicle);
    }

    @Test
    @DisplayName("Restricted mana can pay a Vehicle spell's colored mana cost")
    void restrictedManaPaysColoredVehicleCost() {
        addHangar(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new ProdigysPrototype()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot pay for Mech Hangar's animation ability")
    void restrictedManaCannotPayActivationCost() {
        addHangar(player1);
        addHangar(player1);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ProdigysPrototype());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 2, null, vehicle.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    private Permanent addHangar(Player player) {
        Permanent hangar = harness.addToBattlefieldAndReturn(player, new MechHangar());
        hangar.setSummoningSick(false);
        return hangar;
    }
}
