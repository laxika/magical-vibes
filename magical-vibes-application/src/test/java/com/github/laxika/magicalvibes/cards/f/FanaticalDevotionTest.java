package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CarrionWall;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FanaticalDevotion.class, CarrionWall.class})
class FanaticalDevotionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature regenerates the target creature")
    void sacrificesCreatureAndRegeneratesTarget() {
        harness.addToBattlefield(player1, new FanaticalDevotion());
        harness.addToBattlefield(player1, new CarrionWall());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CarrionWall());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Carrion Wall");
        harness.assertOnBattlefield(player1, "Fanatical Devotion");

        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Chooses which creature to sacrifice when multiple are available")
    void choosesCreatureToSacrifice() {
        harness.addToBattlefield(player1, new FanaticalDevotion());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CarrionWall());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CarrionWall());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CarrionWall());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());

        harness.assertInGraveyard(player1, "Carrion Wall");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(survivor);

        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new FanaticalDevotion());
        harness.addToBattlefield(player1, new CarrionWall());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void requiresCreatureSacrifice() {
        harness.addToBattlefield(player1, new FanaticalDevotion());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CarrionWall());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
