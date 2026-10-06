package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceTheMindSculptor;
import com.github.laxika.magicalvibes.cards.l.LeatherbackBaloth;
import com.github.laxika.magicalvibes.cards.t.TectonicEdge;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SearingBlaze.class, Forest.class, GrizzlyBears.class, JaceTheMindSculptor.class,
        LeatherbackBaloth.class, TectonicEdge.class, Smother.class})
class SearingBlazeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to the targeted player and creature without landfall")
    void dealsOneDamageWithoutLandfall() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId(), creature.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 3 damage to both targets after a land entered under the controller's control")
    void dealsThreeDamageWithLandfall() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new SearingBlaze()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId(), creature.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Allows targeting a creature controlled by the targeted player")
    void targetsCreatureControlledByTargetedPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, List.of(player1.getId(), creature.getId()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rejects a creature controlled by another player")
    void rejectsCreatureControlledByAnotherPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damagesPlaneswalkerAndItsControllersCreatureWithoutLandfall() {
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new JaceTheMindSculptor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, List.of(planeswalker.getId(), creature.getId()));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    void landfallDealsThreeToPlaneswalkerAndCreature() {
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new JaceTheMindSculptor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setHand(player1, List.of(new TectonicEdge(), new SearingBlaze()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, List.of(planeswalker.getId(), creature.getId()));

        harness.assertNotOnBattlefield(player2, "Jace, the Mind Sculptor");
        harness.assertInGraveyard(player2, "Jace, the Mind Sculptor");
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    void rejectsCreatureNotControlledByPlaneswalkersController() {
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new JaceTheMindSculptor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LeatherbackBaloth());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(planeswalker.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresBothTargetsWhenCasting() {
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsCreatureAsFirstTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsLandDoesNotEnableLandfall() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.enterBattlefieldAndReturn(player2, new TectonicEdge());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId(), creature.getId()));

        harness.assertLife(player2, 19);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void nonlandEnteringDoesNotEnableLandfall() {
        harness.enterBattlefieldAndReturn(player1, new LeatherbackBaloth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId(), creature.getId()));

        harness.assertLife(player2, 19);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void landfallIsCheckedAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.enterBattlefieldAndReturn(player1, new TectonicEdge());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void stillDamagesPlayerWhenCreatureIsDestroyedInResponse() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.setHand(player2, List.of(new Smother()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Leatherback Baloth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stillDamagesCreatureWhenPlaneswalkerLeavesBeforeResolution() {
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new JaceTheMindSculptor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, List.of(planeswalker.getId(), creature.getId()));
        planeswalker.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Jace, the Mind Sculptor");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void changedCreatureControllerMakesOnlyCreatureTargetIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, List.of(player2.getId(), creature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void landfallRemainsEnabledAfterTheLandIsSacrificed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LeatherbackBaloth());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new TectonicEdge());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new TectonicEdge());
        }
        harness.setHand(player1, List.of(new TectonicEdge(), new SearingBlaze()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.activateAbility(player1, 0, 1, null, opposingLand.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Tectonic Edge");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Tectonic Edge");
    }
}
