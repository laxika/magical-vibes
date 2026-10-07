package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CoastalTower;
import com.github.laxika.magicalvibes.cards.k.KavuClimber;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThornscapeMaster.class, KavuClimber.class, CoastalTower.class})
class ThornscapeMasterTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability deals 2 damage to a target creature")
    void dealsTwoDamageToTargetCreature() {
        Permanent source = addCreatureReady(player1, new ThornscapeMaster());
        Permanent target = addCreatureReady(player2, new KavuClimber());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability grants chosen-color protection to a target creature until end of turn")
    void grantsChosenProtectionUntilEndOfTurn() {
        Permanent source = addCreatureReady(player1, new ThornscapeMaster());
        Permanent target = addCreatureReady(player2, new KavuClimber());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());

        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Neither ability can target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new ThornscapeMaster());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CoastalTower());

        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                0,
                0,
                null,
                land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 2);
        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                0,
                1,
                null,
                land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Neither ability can be activated while Thornscape Master is tapped")
    void cannotActivateWhenTapped() {
        Permanent source = addCreatureReady(player1, new ThornscapeMaster());
        Permanent target = addCreatureReady(player2, new KavuClimber());
        source.tap();

        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.addMana(player1, ManaColor.WHITE, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void damageResolvesAfterSourceDies() {
        Permanent source = addCreatureReady(player1, new ThornscapeMaster());
        Permanent target = addCreatureReady(player2, new KavuClimber());
        addCreatureReady(player2, new ThornscapeMaster());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player2, 1, 0, null, source.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thornscape Master");
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void protectionFromGreenMakesPendingDamageTargetIllegal() {
        addCreatureReady(player1, new ThornscapeMaster());
        addCreatureReady(player1, new ThornscapeMaster());
        Permanent target = addCreatureReady(player2, new KavuClimber());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.activateAbility(player1, 1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.GREEN.name());
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.GREEN)).isTrue();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void neitherAbilityCanBeActivatedWithOnlyOneManaOfItsColor() {
        Permanent source = addCreatureReady(player1, new ThornscapeMaster());
        Permanent target = addCreatureReady(player2, new KavuClimber());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void neitherAbilityCanBeActivatedWhileSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ThornscapeMaster());
        Permanent target = addCreatureReady(player2, new KavuClimber());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
