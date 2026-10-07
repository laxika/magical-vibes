package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TelJiladLifebreather.class, Forest.class, GrizzlyBears.class, FountainOfYouth.class})
class TelJiladLifebreatherTest extends BaseCardTest {

    @Test
    void canTargetItselfAndPaysCostsBeforeResolution() {
        Permanent lifebreather = addCreatureReady(player1, new TelJiladLifebreather());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, lifebreather.getId());

        assertThat(lifebreather.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(lifebreather.getRegenerationShield()).isZero();

        harness.passBothPriorities();

        assertThat(lifebreather.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent lifebreather = harness.addToBattlefieldAndReturn(player1, new TelJiladLifebreather());
        lifebreather.setSummoningSick(true);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, lifebreather.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lifebreather.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void creatingShieldDoesNotImmediatelyTapOrRemoveDamage() {
        addCreatureReady(player1, new TelJiladLifebreather());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setMarkedDamage(1);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void shieldSavesCreatureFromLethalCombatDamageAndRemovesItFromCombat() {
        addCreatureReady(player1, new TelJiladLifebreather());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new TelJiladLifebreather());
        attacker.setAttacking(true);
        target.setBlocking(true);
        target.addBlockingTarget(0);
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.isBlocking()).isFalse();
        assertThat(target.getBlockingTargets()).isEmpty();
        harness.assertInGraveyard(player2, "Tel-Jilad Lifebreather");
    }

    @Test
    void sacrificesAForestAndRegeneratesTargetCreature() {
        Permanent lifebreather = addCreatureReady(player1, new TelJiladLifebreather());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(lifebreather.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new TelJiladLifebreather());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void cannotActivateWithoutAForest() {
        addCreatureReady(player1, new TelJiladLifebreather());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutGreenMana() {
        Permanent lifebreather = addCreatureReady(player1, new TelJiladLifebreather());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lifebreather.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void cannotActivateWhenAlreadyTapped() {
        Permanent lifebreather = addCreatureReady(player1, new TelJiladLifebreather());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        lifebreather.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void cannotSacrificeAnOpponentsForest() {
        addCreatureReady(player1, new TelJiladLifebreather());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNothingIfTargetLeavesBeforeResolution() {
        addCreatureReady(player1, new TelJiladLifebreather());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Forest");
    }
}
