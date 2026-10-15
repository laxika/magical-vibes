package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OboroBreezecaller.class, OboroEnvoy.class, OboroPalaceInTheClouds.class})
class OboroBreezecallerTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as a cost and untaps the target land")
    void returnsLandAndUntapsTargetLand() {
        harness.addToBattlefield(player1, new OboroBreezecaller());
        harness.addToBattlefield(player1, new OboroPalaceInTheClouds());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OboroPalaceInTheClouds());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Oboro, Palace in the Clouds");
        harness.assertNotOnBattlefield(player1, "Oboro, Palace in the Clouds");
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLandToReturn() {
        harness.addToBattlefield(player1, new OboroBreezecaller());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OboroPalaceInTheClouds());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can return the targeted land to pay the cost")
    void canReturnTargetedLandAsCost() {
        harness.addToBattlefield(player1, new OboroBreezecaller());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OboroPalaceInTheClouds());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInHand(player1, "Oboro, Palace in the Clouds");
        harness.assertNotOnBattlefield(player1, "Oboro, Palace in the Clouds");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Oboro, Palace in the Clouds");
    }

    @Test
    @DisplayName("A tapped Breezecaller can untap an already untapped land")
    void tappedSourceCanTargetUntappedLand() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new OboroBreezecaller());
        source.tap();
        harness.addToBattlefield(player1, new OboroPalaceInTheClouds());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OboroPalaceInTheClouds());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Oboro, Palace in the Clouds");
        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with only one mana")
    void cannotActivateWithInsufficientMana() {
        harness.addToBattlefield(player1, new OboroBreezecaller());
        harness.addToBattlefield(player1, new OboroPalaceInTheClouds());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OboroPalaceInTheClouds());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Oboro, Palace in the Clouds");
        harness.assertNotInHand(player1, "Oboro, Palace in the Clouds");
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player1, new OboroBreezecaller());
        harness.addToBattlefield(player1, new OboroPalaceInTheClouds());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OboroEnvoy());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pays the cost even when the target leaves before resolution")
    void paysCostWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new OboroBreezecaller());
        harness.addToBattlefield(player1, new OboroPalaceInTheClouds());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OboroPalaceInTheClouds());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Oboro, Palace in the Clouds");
        harness.assertNotOnBattlefield(player1, "Oboro, Palace in the Clouds");
    }
}
