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
