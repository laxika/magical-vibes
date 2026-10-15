package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BrightfieldMustang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.k.KeenBuccaneer;
import com.github.laxika.magicalvibes.cards.t.ThunderousVelocipede;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RunOver.class, BrightfieldMustang.class, GrizzlyBears.class, LlanowarElves.class,
        KeenBuccaneer.class, ThunderousVelocipede.class})
class RunOverTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {G} and deals Mount's power to an opponent's creature")
    void costsLessWhenTargetingMountYouControl() {
        harness.addToBattlefield(player1, new BrightfieldMustang());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID mustangId = harness.getPermanentId(player1, "Brightfield Mustang");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, List.of(mustangId, elvesId));

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Requires the full cost when the first target is not a Mount or Vehicle")
    void doesNotReduceCostForOtherCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearsId, elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature you control as the second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void fullCostDealsExactPowerWithoutReturnDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KeenBuccaneer());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new BrightfieldMustang());
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), victim.getId()));

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Keen Buccaneer");
        harness.assertOnBattlefield(player2, "Brightfield Mustang");
        harness.assertInGraveyard(player1, "Run Over");
    }

    @Test
    void opponentsMountDoesNotReduceCost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KeenBuccaneer());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new BrightfieldMustang());
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void crewedVehicleReducesCost() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ThunderousVelocipede());
        harness.addToBattlefield(player1, new BrightfieldMustang());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new BrightfieldMustang());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, List.of(vehicle.getId(), victim.getId()));

        harness.assertInGraveyard(player2, "Brightfield Mustang");
        assertThat(vehicle.getMarkedDamage()).isZero();
    }

    @Test
    void uncrewedVehicleIsNotALegalSource() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ThunderousVelocipede());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new BrightfieldMustang());
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(vehicle.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotUseOpponentsCreatureAsDamageSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new BrightfieldMustang());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new KeenBuccaneer());
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), victim.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageUsesPowerAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KeenBuccaneer());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new BrightfieldMustang());
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));

        source.setPersistentPowerModifier(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Brightfield Mustang");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    void missingSourceDoesNotDealDamageUsingLastKnownPower() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BrightfieldMustang());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new KeenBuccaneer());
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Keen Buccaneer");
        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Run Over");
    }

    @Test
    void sourceChangingControllerBeforeResolutionDealsNoDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BrightfieldMustang());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new KeenBuccaneer());
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Keen Buccaneer");
        harness.assertInGraveyard(player1, "Run Over");
    }

    @Test
    void missingVictimDoesNotDamageSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BrightfieldMustang());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new KeenBuccaneer());
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(victim);
        harness.passBothPriorities();

        assertThat(source.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Brightfield Mustang");
        harness.assertInGraveyard(player1, "Run Over");
    }

    @Test
    void negativePowerDealsNoDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KeenBuccaneer());
        source.setPersistentPowerModifier(-3);
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new BrightfieldMustang());
        harness.setHand(player1, List.of(new RunOver()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), victim.getId()));

        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Brightfield Mustang");
    }
}
