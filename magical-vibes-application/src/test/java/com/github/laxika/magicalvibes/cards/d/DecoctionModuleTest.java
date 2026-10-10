package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DecoctionModule.class, GrizzlyBears.class, Forest.class, MarchOfTheMachines.class, ControlMagic.class})
class DecoctionModuleTest extends BaseCardTest {

    @Test
    void givesEnergyWhenCreatureYouControlEnters() {
        harness.addToBattlefield(player1, new DecoctionModule());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void doesNotGiveEnergyWhenOpponentCreatureEnters() {
        harness.addToBattlefield(player1, new DecoctionModule());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void paysFourManaAndReturnsTargetCreatureYouControl() {
        Permanent module = harness.addToBattlefieldAndReturn(player1, new DecoctionModule());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(module.isTapped()).isTrue();
    }

    @Test
    void cannotTargetCreatureOpponentControls() {
        harness.addToBattlefield(player1, new DecoctionModule());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new DecoctionModule());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void energyIsGrantedOnlyWhenTheTriggerResolves() {
        harness.addToBattlefield(player1, new DecoctionModule());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void eachModuleTriggersEvenWhenTapped() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DecoctionModule());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DecoctionModule());
        first.tap();
        second.tap();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void cannotActivateWithOnlyThreeMana() {
        Permanent module = harness.addToBattlefieldAndReturn(player1, new DecoctionModule());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(module.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateWhenModuleIsTapped() {
        Permanent module = harness.addToBattlefieldAndReturn(player1, new DecoctionModule());
        module.tap();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void returnsBorrowedCreatureToItsOwnersHand() {
        harness.addToBattlefield(player1, new DecoctionModule());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ControlMagic()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    void targetBecomingOpponentControlledMakesTheAbilityFailToResolve() {
        harness.addToBattlefield(player1, new DecoctionModule());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    @Test
    void triggersForAnArtifactEnteringAsACreature() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.addToBattlefield(player1, new DecoctionModule());
        harness.setHand(player1, List.of(new DecoctionModule()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void triggersForItselfWhenEnteringAsACreature() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.setHand(player1, List.of(new DecoctionModule()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }
}
