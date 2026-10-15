package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakdosIckspitter.class, AssaultZeppelid.class, AzoriusSignet.class})
class RakdosIckspitterTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a creature and its controller loses 1 life")
    void damagesCreatureAndItsControllerLosesLife() {
        Permanent ickspitter = addCreatureReady(player1, new RakdosIckspitter());
        Permanent target = addCreatureReady(player2, new AssaultZeppelid());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(ickspitter.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The target's controller loses life even when that is the ability's controller")
    void targetsControllerLosesLifeWhenTargetIsControlledByAbilityController() {
        addCreatureReady(player1, new RakdosIckspitter());
        Permanent target = addCreatureReady(player1, new AssaultZeppelid());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The controller's life loss is not prevented by a damage prevention shield")
    void controllerLifeLossIsNotPreventedByDamagePrevention() {
        addCreatureReady(player1, new RakdosIckspitter());
        Permanent target = addCreatureReady(player2, new AssaultZeppelid());
        harness.setLife(player2, 20);
        gd.playerDamagePreventionShields.put(player2.getId(), 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller loses life even when the damage destroys the target")
    void controllerLosesLifeWhenDamageDestroysTarget() {
        addCreatureReady(player1, new RakdosIckspitter());
        Permanent target = addCreatureReady(player2, new RakdosIckspitter());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rakdos Ickspitter");
        harness.assertInGraveyard(player2, "Rakdos Ickspitter");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new RakdosIckspitter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AzoriusSignet());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("An ability with a target that leaves the battlefield causes no further life loss")
    void doesNotLoseLifeWhenTargetLeavesBeforeResolution() {
        addCreatureReady(player1, new RakdosIckspitter());
        Permanent target = addCreatureReady(player2, new RakdosIckspitter());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Rakdos Ickspitter");
        harness.assertLife(player2, 19);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability still deals damage and causes life loss after its source dies")
    void resolvesAfterSourceDies() {
        Permanent source = addCreatureReady(player1, new RakdosIckspitter());
        addCreatureReady(player2, new RakdosIckspitter());
        Permanent target = addCreatureReady(player2, new AssaultZeppelid());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player2, 0, null, source.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rakdos Ickspitter");
        harness.assertLife(player1, 19);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate the tap ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new RakdosIckspitter());
        Permanent target = addCreatureReady(player2, new AssaultZeppelid());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot pay the tap cost twice without untapping")
    void cannotActivateAgainWhileTapped() {
        addCreatureReady(player1, new RakdosIckspitter());
        Permanent target = addCreatureReady(player2, new AssaultZeppelid());
        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
    }
}
